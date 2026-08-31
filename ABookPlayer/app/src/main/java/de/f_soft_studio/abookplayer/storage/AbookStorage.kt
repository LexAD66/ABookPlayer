package de.f_soft_studio.abookplayer.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Chapter
import de.f_soft_studio.abookplayer.domain.model.ExportState
import de.f_soft_studio.abookplayer.util.AbookModels
import de.f_soft_studio.abookplayer.util.ChapterDurations
import de.f_soft_studio.abookplayer.util.ComparisonType
import de.f_soft_studio.abookplayer.util.DuplicateDetector
import de.f_soft_studio.abookplayer.util.DuplicateMatch
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * AbookStorage: Scannt Standard- und Benutzer-Ordner nach .abook, .zip und ungepackten Hörbuch-Strukturen,
 * unterstützt SAF-Uri-Import (Dateien & Ordner), verarbeitet kanonische manifest.json (Format v1) sowie legacy metadata.json,
 * sichert Zip-Bomb & Zip-Slip ab und speichert Stammdaten im Repository.
 */
class AbookStorage(
    private val context: Context,
    private val repository: AudiobookRepository
) {
    companion object {
        const val MAX_SINGLE_FILE_SIZE_BYTES = 4L * 1024 * 1024 * 1024 // 4 GB für große Hörbuchdateien/M4B
        const val MAX_TOTAL_UNCOMPRESSED_SIZE_BYTES = 20L * 1024 * 1024 * 1024 // 20 GB für große Archive
        const val MAX_COMPRESSION_RATIO = 100L

        fun getPublicStorageDir(context: Context): File {
            return LibraryLocationManager.getLibraryDir(context)
        }
    }

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val folderScanner = FolderScanner(context)

    val lastDetectedDuplicates = mutableListOf<DuplicateMatch>()

    fun getDetectedDuplicates(): List<DuplicateMatch> = lastDetectedDuplicates.toList()

    fun clearDetectedDuplicates() {
        lastDetectedDuplicates.clear()
    }

    suspend fun deleteDuplicateFromStorage(match: DuplicateMatch): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = if (match.isSafUri || match.candidatePath.startsWith("content://")) {
                val doc = DocumentFile.fromSingleUri(context, Uri.parse(match.candidatePath))
                    ?: DocumentFile.fromTreeUri(context, Uri.parse(match.candidatePath))
                doc?.delete() ?: false
            } else {
                val file = File(match.candidatePath)
                if (file.exists()) {
                    file.deleteRecursively()
                } else false
            }
            if (result) {
                lastDetectedDuplicates.removeAll { it.candidatePath == match.candidatePath }
            }
            result
        } catch (e: Exception) {
            Log.e("AbookStorage", "Fehler beim Löschen des Doppelgängers: ${e.message}", e)
            false
        }
    }

    fun saveScannedFolderUri(uriString: String) {
        val prefs = context.getSharedPreferences("abook_storage_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("scanned_saf_uris", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(uriString)
        prefs.edit().putStringSet("scanned_saf_uris", set).apply()
    }

    fun removeScannedFolderUri(uriString: String) {
        val prefs = context.getSharedPreferences("abook_storage_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("scanned_saf_uris", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.remove(uriString)
        prefs.edit().putStringSet("scanned_saf_uris", set).apply()
    }

    fun getScannedFolderUris(): List<String> {
        val prefs = context.getSharedPreferences("abook_storage_prefs", Context.MODE_PRIVATE)
        return prefs.getStringSet("scanned_saf_uris", emptySet())?.toList() ?: emptyList()
    }

    suspend fun scanAndImport(additionalFolderPaths: List<String> = emptyList()): List<Audiobook> = withContext(Dispatchers.IO) {
        val primaryLibDir = LibraryLocationManager.getLibraryDir(context)
        val directoriesToScan = mutableListOf<File>(primaryLibDir)

        val allAdditional = (additionalFolderPaths + getScannedFolderUris()).distinct()
        for (path in allAdditional) {
            if (path.startsWith("content://")) {
                try {
                    importFromFolderUri(Uri.parse(path))
                } catch (e: Exception) {
                    Log.e("AbookStorage", "Fehler beim Scannen der SAF-Uri $path: ${e.message}")
                }
            } else {
                val f = File(path)
                if (f.exists() && f.isDirectory && !directoriesToScan.contains(f) && !FolderScanner.isIgnoredDirectory(f)) {
                    directoriesToScan.add(f)
                }
            }
        }

        val filesToScan = mutableListOf<File>()
        for (dir in directoriesToScan) {
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles { f -> f.isFile && (f.extension.equals("abook", true) || f.extension.equals("zip", true)) }
                    ?.let { filesToScan.addAll(it) }
            }
        }


        val imported = mutableListOf<Audiobook>()
        val existingBooks = try { repository.getAllAudiobooks().first() } catch (_: Exception) { emptyList() }

        // 1. .abook und .zip Archive verarbeiten
        filesToScan.distinctBy { it.absolutePath }.forEach { zipFile ->
            if (existingBooks.none { it.filePath == zipFile.absolutePath }) {
                val book = parseAndSaveAbook(zipFile)
                if (book != null) {
                    imported.add(book)
                }
            }
        }

        // 2. Ungepackte Hörbuchordner und CD1/CD2 Unterordner verarbeiten
        for (dir in directoriesToScan) {
            scanUnpackedFolderInternal(dir, existingBooks, imported)
        }

        imported
    }

    private suspend fun scanUnpackedFolderInternal(
        baseDir: File,
        existingBooks: List<Audiobook>,
        importedList: MutableList<Audiobook>
    ) {
        if (!baseDir.exists() || !baseDir.isDirectory) return
        val scannedResults = folderScanner.scanDirectory(baseDir)
        val allCurrent = (existingBooks + importedList).distinctBy { it.id }

        for (result in scannedResults) {
            var book = result.audiobook
            val chapters = result.chapters

            var isIdenticalDuplicate = false
            var isDifferentVersion = false
            var matchBook: Audiobook? = null

            for (eb in allCurrent) {
                if (eb.filePath == book.filePath) {
                    isIdenticalDuplicate = true
                    break
                }
                val comp = DuplicateDetector.compare(
                    newTitle = book.title,
                    newAuthor = book.author,
                    newDurationMs = book.duration,
                    newChapterCount = chapters.size,
                    newNarrator = book.narrator,
                    existingBook = eb,
                    existingChapterCount = 0
                )
                if (comp == ComparisonType.IDENTICAL_DUPLICATE) {
                    isIdenticalDuplicate = true
                    matchBook = eb
                    break
                } else if (comp == ComparisonType.DIFFERENT_VERSION) {
                    isDifferentVersion = true
                    matchBook = eb
                }
            }

            if (isIdenticalDuplicate) {
                matchBook?.let { existing ->
                    if (existing.filePath != book.filePath) {
                        val match = DuplicateMatch(
                            existingAudiobook = existing,
                            candidateTitle = book.title,
                            candidatePath = book.filePath,
                            comparisonType = ComparisonType.IDENTICAL_DUPLICATE,
                            durationDifferenceMs = abs(book.duration - existing.duration)
                        )
                        if (lastDetectedDuplicates.none { it.candidatePath == book.filePath }) {
                            lastDetectedDuplicates.add(match)
                        }
                    }
                }
                Log.d("AbookStorage", "Identisches Duplikat übersprungen: ${book.title} (${book.filePath})")
            } else {
                if (isDifferentVersion) {
                    val versionLabel = if (!book.narrator.isNullOrBlank()) " (${book.narrator})" else " (Edition)"
                    book = book.copy(title = "${book.title}$versionLabel")
                }
                val bookId = repository.saveAudiobook(book)
                val updatedChapters = result.chapters.map { it.copy(audiobookId = bookId) }
                repository.saveChapters(updatedChapters)

                var finalCoverUri = book.coverUri
                var finalDescription = book.description
                if (finalCoverUri.isNullOrBlank()) {
                    val isAutoFetch = context.getSharedPreferences("abook_settings_prefs", Context.MODE_PRIVATE).getBoolean("auto_online_cover_fetch", true)
                    if (isAutoFetch) {
                        try {
                            val scraper = OnlineCoverScraper(context)
                            val onlineCover = scraper.searchCoverAndMetadata(book.title, book.author)
                            if (onlineCover != null && !onlineCover.coverPath.isNullOrBlank()) {
                                repository.updateCoverAndDescription(bookId, onlineCover.coverPath, onlineCover.description)
                                finalCoverUri = onlineCover.coverPath
                                finalDescription = onlineCover.description
                            }
                        } catch (e: Exception) {
                            Log.d("AbookStorage", "Auto online cover fetch failed: ${e.message}")
                        }
                    }
                }
                importedList.add(book.copy(id = bookId, coverUri = finalCoverUri, description = finalDescription))
            }
        }
    }

    /**
     * Importiert eine .abook / .zip / Audiobook-Datei über SAF.
     */
    suspend fun importFromUri(uri: Uri): Audiobook? = withContext(Dispatchers.IO) {
        return@withContext try {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                Log.d("AbookStorage", "Persistable URI permission nicht verfügbar: ${e.message}")
            }

            val fileName = getFileNameFromUri(uri) ?: "imported_${System.currentTimeMillis()}"
            val ext = File(fileName).extension.lowercase()
            val audiobooksDir = getPublicStorageDir(context)
            val destFile = File(audiobooksDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                if (!copyStreamSafely(input, destFile, maxSizeBytes = MAX_TOTAL_UNCOMPRESSED_SIZE_BYTES)) {
                    Log.e("AbookStorage", "Fehler bei der Übertragung aus SAF-Uri")
                    return@withContext null
                }
            } ?: return@withContext null

            if (ext == "abook" || ext == "zip") {
                parseAndSaveAbook(destFile)
            } else if (isAudioFile(fileName)) {
                val duration = ChapterDurations.readDurationMs(destFile)
                val title = destFile.nameWithoutExtension
                val book = Audiobook(
                    title = title,
                    author = "",
                    filePath = destFile.absolutePath,
                    duration = duration,
                    lastPlayed = System.currentTimeMillis()
                )
                val bookId = repository.saveAudiobook(book)
                val ch = Chapter(audiobookId = bookId, title = title, startTime = 0L, audioPath = destFile.absolutePath)
                repository.saveChapters(listOf(ch))
                book.copy(id = bookId)
            } else {
                parseAndSaveAbook(destFile)
            }
        } catch (e: Exception) {
            Log.e("AbookStorage", "Fehler beim SAF-Import aus Uri $uri: ${e.message}", e)
            null
        }
    }

    /**
     * Importiert einen ganzen Ordner per SAF-TreeUri (DocumentFile).
     */
    suspend fun importFromFolderUri(treeUri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
            } catch (_: Exception) {}

            val docFile = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext 0
            val existingBooks = try { repository.getAllAudiobooks().first() } catch (_: Exception) { emptyList() }
            val imported = mutableListOf<Audiobook>()

            processDocumentFileRecursive(docFile, existingBooks, imported)
            return@withContext imported.size
        } catch (e: Exception) {
            Log.e("AbookStorage", "Fehler beim SAF-Ordner-Import: ${e.message}", e)
            return@withContext 0
        }
    }

    private suspend fun processDocumentFileRecursive(
        dir: DocumentFile,
        existingBooks: List<Audiobook>,
        imported: MutableList<Audiobook>
    ) {
        if (!dir.isDirectory) return
        val children = dir.listFiles()

        // 1. .abook / .zip Dateien im Ordner importieren
        val zipFiles = children.filter { f -> f.isFile && (f.name?.lowercase()?.endsWith(".abook") == true || f.name?.lowercase()?.endsWith(".zip") == true) }
        for (zipDoc in zipFiles) {
            importFromUri(zipDoc.uri)?.let { imported.add(it) }
        }

        // 2. Audiodateien und CD1/CD2 Unterordner verarbeiten
        val audioFiles = children.filter { f -> f.isFile && isAudioFile(f.name ?: "") }
        val subDirs = children.filter { it.isDirectory }
        val discSubfolders = subDirs.filter { f -> FolderScanner.isDiscSubfolder(File(f.name ?: "")) }

        if (discSubfolders.isNotEmpty() || audioFiles.isNotEmpty()) {
            val bookTitle = dir.name ?: "Importiertes Hörbuch"
            val allCurrent = (existingBooks + imported).distinctBy { it.id }
            var isIdenticalDuplicate = false
            var isDifferentVersion = false
            var matchBook: Audiobook? = null

            for (eb in allCurrent) {
                if (eb.filePath == dir.uri.toString()) {
                    isIdenticalDuplicate = true
                    break
                }
                val comp = DuplicateDetector.compare(
                    newTitle = bookTitle,
                    newAuthor = "",
                    newDurationMs = 0L,
                    newChapterCount = 0,
                    newNarrator = null,
                    existingBook = eb,
                    existingChapterCount = 0
                )
                if (comp == ComparisonType.IDENTICAL_DUPLICATE) {
                    isIdenticalDuplicate = true
                    matchBook = eb
                    break
                } else if (comp == ComparisonType.DIFFERENT_VERSION) {
                    isDifferentVersion = true
                    matchBook = eb
                }
            }

            if (isIdenticalDuplicate) {
                matchBook?.let { existing ->
                    if (existing.filePath != dir.uri.toString()) {
                        val match = DuplicateMatch(
                            existingAudiobook = existing,
                            candidateTitle = bookTitle,
                            candidatePath = dir.uri.toString(),
                            comparisonType = ComparisonType.IDENTICAL_DUPLICATE,
                            durationDifferenceMs = 0L,
                            isSafUri = true
                        )
                        if (lastDetectedDuplicates.none { it.candidatePath == dir.uri.toString() }) {
                            lastDetectedDuplicates.add(match)
                        }
                    }
                }
                Log.d("AbookStorage", "Identisches SAF-Duplikat übersprungen: $bookTitle")
            } else {
                val finalTitle = if (isDifferentVersion) "$bookTitle (Edition)" else bookTitle
                val bookDir = File(getPublicStorageDir(context), "imported_${finalTitle.hashCode()}").apply { if (!exists()) mkdirs() }
                val chapters = mutableListOf<Chapter>()
                var totalDuration = 0L
                val allDocAudioFiles = mutableListOf<Pair<String, DocumentFile>>()

                if (discSubfolders.isNotEmpty()) {
                    for (disc in discSubfolders.sortedBy { it.name }) {
                        disc.listFiles().filter { it.isFile && isAudioFile(it.name ?: "") }
                            .sortedBy { it.name }
                            .forEach { f -> allDocAudioFiles.add(Pair("${disc.name} - ${f.name}", f)) }
                    }
                } else {
                    audioFiles.sortedBy { it.name }
                        .forEach { f -> allDocAudioFiles.add(Pair(f.name ?: "Kapitel", f)) }
                }

                for ((chTitle, doc) in allDocAudioFiles) {
                    val targetFile = File(bookDir, doc.name ?: "track.mp3")
                    if (!targetFile.exists() || targetFile.length() == 0L) {
                        context.contentResolver.openInputStream(doc.uri)?.use { input ->
                            copyStreamSafely(input, targetFile)
                        }
                    }
                    val dur = ChapterDurations.readDurationMs(targetFile)
                    chapters.add(Chapter(audiobookId = 0L, title = chTitle, startTime = totalDuration, audioPath = targetFile.absolutePath))
                    totalDuration += dur
                }

                if (chapters.isNotEmpty()) {
                    var coverUri: String? = null
                    val imageDocs = children.filter { f -> f.isFile && f.name?.lowercase()?.let { ext -> ext.endsWith(".jpg") || ext.endsWith(".png") || ext.endsWith(".jpeg") || ext.endsWith(".webp") } == true }
                    val coverCandidate = imageDocs.firstOrNull { f ->
                        val name = f.name?.lowercase() ?: ""
                        name.startsWith("cover") || name.startsWith("folder") || name.startsWith("front")
                    } ?: imageDocs.firstOrNull()

                    if (coverCandidate != null) {
                        val targetCover = File(bookDir, coverCandidate.name ?: "cover.jpg")
                        if (!targetCover.exists()) {
                            context.contentResolver.openInputStream(coverCandidate.uri)?.use { input ->
                                copyStreamSafely(input, targetCover)
                            }
                        }
                        coverUri = targetCover.absolutePath
                    }

                    if (coverUri == null) {
                        for (ch in chapters.take(10)) {
                            try {
                                val retriever = android.media.MediaMetadataRetriever()
                                retriever.setDataSource(ch.audioPath)
                                val art = retriever.embeddedPicture
                                retriever.release()
                                if (art != null && art.isNotEmpty()) {
                                    val coversDir = LibraryLocationManager.getCoversDir(context)
                                    val targetFile = File(coversDir, "cover_embedded_${bookDir.name.hashCode()}.jpg")

                                    targetFile.writeBytes(art)
                                    coverUri = targetFile.absolutePath
                                    break
                                }
                            } catch (_: Exception) {}
                        }
                    }

                    val book = Audiobook(
                        title = finalTitle,
                        author = "",
                        filePath = bookDir.absolutePath,
                        coverUri = coverUri,
                        duration = totalDuration,
                        lastPlayed = System.currentTimeMillis()
                    )
                    val bookId = repository.saveAudiobook(book)
                    repository.saveChapters(chapters.map { it.copy(audiobookId = bookId) })
                    imported.add(book.copy(id = bookId))
                    return
                }
            }
        }

        // 3. Rekursiv in Unterordner wechseln
        for (subDir in subDirs) {
            if (!FolderScanner.isDiscSubfolder(File(subDir.name ?: ""))) {
                processDocumentFileRecursive(subDir, existingBooks, imported)
            }
        }
    }

    /**
     * Exportiert ein bestehendes Hörbuch samt Kapiteln und Cover als kanonische .abook-ZIP-Datei.
     */
    fun exportToAbook(
        audiobook: Audiobook,
        chapters: List<Chapter>,
        outputStream: OutputStream
    ): Flow<ExportState> = flow {
        emit(ExportState.Exporting(0))
        try {
            ZipOutputStream(outputStream).use { zos ->
                val tracks = chapters.mapIndexed { index, ch ->
                    val fileExt = ch.audioPath?.let { File(it).extension }?.ifBlank { "mp3" } ?: "mp3"
                    val relativeAudioPath = "audio/%03d-kapitel.%s".format(index + 1, fileExt)
                    val durationMs = if (index < chapters.size - 1) {
                        chapters[index + 1].startTime - ch.startTime
                    } else {
                        (audiobook.duration - ch.startTime).coerceAtLeast(0L)
                    }
                    de.f_soft_studio.abookplayer.util.AbookModels.TrackRaw(
                        file = relativeAudioPath,
                        title = ch.title,
                        duration = durationMs
                    )
                }

                val coverFileName = audiobook.coverUri?.let { uriStr ->
                    val f = File(uriStr)
                    if (f.exists()) "cover.${f.extension.ifBlank { "jpg" }}" else null
                }

                val manifestRaw = de.f_soft_studio.abookplayer.util.AbookModels.AbookManifestRaw(
                    version = 1,
                    id = "book_${audiobook.id}_${audiobook.title.hashCode()}",
                    title = audiobook.title,
                    author = audiobook.author,
                    narrator = audiobook.narrator,
                    description = audiobook.description,
                    cover = coverFileName,
                    totalDuration = audiobook.duration,
                    tracks = tracks
                )

                val manifestJsonStr = json.encodeToString(de.f_soft_studio.abookplayer.util.AbookModels.AbookManifestRaw.serializer(), manifestRaw)

                val manifestEntry = ZipEntry("manifest.json")
                zos.putNextEntry(manifestEntry)
                zos.write(manifestJsonStr.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                if (coverFileName != null && audiobook.coverUri != null) {
                    val coverFile = File(audiobook.coverUri)
                    if (coverFile.exists()) {
                        val coverEntry = ZipEntry(coverFileName)
                        zos.putNextEntry(coverEntry)
                        coverFile.inputStream().use { input -> input.copyTo(zos) }
                        zos.closeEntry()
                    }
                }

                val totalTracks = chapters.size.coerceAtLeast(1)
                chapters.forEachIndexed { index, ch ->
                    val audioPath = ch.audioPath
                    if (audioPath != null) {
                        val srcFile = File(audioPath)
                        if (srcFile.exists()) {
                            val trackRaw = tracks[index]
                            val entry = ZipEntry(trackRaw.file)
                            zos.putNextEntry(entry)
                            srcFile.inputStream().use { input -> input.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                    val progress = ((index + 1).toFloat() / totalTracks * 100).toInt()
                    emit(ExportState.Exporting(progress))
                }
            }
            emit(ExportState.Success("Erfolgreich als .abook exportiert"))
        } catch (e: Exception) {
            Log.e("AbookStorage", "Fehler beim Exportieren von .abook: ${e.message}", e)
            emit(ExportState.Error("Export fehlgeschlagen: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun parseAndSaveAbook(zipFile: File): Audiobook? = withContext(Dispatchers.IO) {
        return@withContext try {
            ZipFile(zipFile).use { zf ->
                val allEntries = zf.entries().asSequence().toList()
                
                // 1. Prüfen auf kanonisches manifest.json oder legacy metadata.json
                val canonicalManifestEntry = allEntries.firstOrNull {
                    val name = File(it.name).name.lowercase()
                    name == "manifest.json"
                }
                val legacyMetaEntry = if (canonicalManifestEntry == null) {
                    allEntries.firstOrNull {
                        val name = File(it.name).name.lowercase()
                        name == "metadata.json"
                    }
                } else null

                val unpackedDir = File(LibraryLocationManager.getTempDir(context), "unpacked_${zipFile.nameWithoutExtension.hashCode()}").apply {
                    if (!exists()) mkdirs()
                }

                var totalExtractedBytes = 0L

                // Extrahieren aller Audio-Dateien (.mp3, .m4a, etc.)
                val audioEntriesMap = mutableMapOf<String, File>()
                allEntries.filter { !it.isDirectory && isAudioFile(it.name) }.forEach { entry ->
                    if (!validateZipEntryRatio(entry)) {
                        Log.w("AbookStorage", "Verdacht auf Zip-Bomb in Eintrag ${entry.name}. Überspringe.")
                        return@forEach
                    }

                    val fileName = File(entry.name).name
                    val extractedFile = safeTargetFile(unpackedDir, fileName)
                    if (extractedFile == null) {
                        Log.w("AbookStorage", "Unsicherer Zip-Eintrag übersprungen: ${entry.name}")
                        return@forEach
                    }
                    if (!extractedFile.exists() || extractedFile.length() == 0L) {
                        zf.getInputStream(entry).use { input ->
                            val success = copyStreamSafely(input, extractedFile)
                            if (!success) {
                                Log.e("AbookStorage", "Fehler beim Entpacken der Audio-Datei: ${entry.name}")
                                return@forEach
                            }
                        }
                    }
                    totalExtractedBytes += extractedFile.length()
                    if (totalExtractedBytes > MAX_TOTAL_UNCOMPRESSED_SIZE_BYTES) {
                        Log.e("AbookStorage", "Gesamtentschlüsselungsgröße überschreitet Limit ($MAX_TOTAL_UNCOMPRESSED_SIZE_BYTES Bytes)")
                        return@use null
                    }

                    audioEntriesMap[fileName.lowercase()] = extractedFile
                    audioEntriesMap[entry.name.lowercase()] = extractedFile
                }

                var title = zipFile.nameWithoutExtension
                var author = ""
                var narrator: String? = null
                var description: String? = null
                var series: String? = null
                var parentSeries: String? = null
                var seriesOrder: Int? = null
                var manifestCoverRelPath: String? = null
                val tempChapterInfos = mutableListOf<Pair<String, File?>>()

                // Kanonischer Manifest-Parser (manifest.json)
                if (canonicalManifestEntry != null) {
                    try {
                        val text = zf.getInputStream(canonicalManifestEntry).bufferedReader().readText()
                        val manifest = json.decodeFromString<AbookModels.AbookManifestRaw>(text)
                        title = manifest.title.ifBlank { title }
                        author = manifest.author ?: ""
                        narrator = manifest.narrator
                        description = manifest.description
                        series = manifest.series
                        parentSeries = manifest.parentSeries
                        seriesOrder = manifest.seriesOrder
                        manifestCoverRelPath = manifest.cover

                        manifest.tracks.forEach { tr ->
                            val chTitle = tr.title ?: File(tr.file).nameWithoutExtension
                            val targetFile = audioEntriesMap[tr.file.lowercase()] ?: audioEntriesMap[File(tr.file).name.lowercase()]
                            tempChapterInfos.add(Pair(chTitle, targetFile))
                        }
                    } catch (e: Exception) {
                        Log.w("AbookStorage", "Fehler beim Lesen der kanonischen manifest.json in ${zipFile.name}: ${e.message}")
                    }
                }
                // Legacy Fallback (metadata.json)
                else if (legacyMetaEntry != null) {
                    try {
                        val text = zf.getInputStream(legacyMetaEntry).bufferedReader().readText()
                        val raw = json.decodeFromString<AbookModels.AbookMetaRaw>(text)
                        title = raw.title.ifBlank { title }
                        author = raw.author ?: ""
                        description = raw.description
                        raw.chapters.forEach { ch ->
                            val chTitle = ch.title ?: File(ch.file).nameWithoutExtension
                            val targetFile = audioEntriesMap[ch.file.lowercase()] ?: audioEntriesMap[File(ch.file).name.lowercase()]
                            tempChapterInfos.add(Pair(chTitle, targetFile))
                        }
                    } catch (e: Exception) {
                        Log.w("AbookStorage", "Fehler beim Lesen von metadata.json in ${zipFile.name}: ${e.message}")
                    }
                }

                // Fallback: Falls keine Kapitel im Manifest/Metadata gefunden wurden
                if (tempChapterInfos.isEmpty()) {
                    val sortedFiles = audioEntriesMap.values.distinctBy { it.absolutePath }.sortedBy { it.name }
                    sortedFiles.forEach { audioFile ->
                        val chapterTitle = audioFile.nameWithoutExtension
                        tempChapterInfos.add(Pair(chapterTitle, audioFile))
                    }
                }

                // Exakte Zeit- und Dauerberechnung mit MediaMetadataRetriever
                var cumulativeTimeMs = 0L
                val chapters = mutableListOf<Chapter>()
                tempChapterInfos.forEach { (chTitle, audioFile) ->
                    val fileDuration = audioFile?.let { ChapterDurations.readDurationMs(it) } ?: 0L
                    chapters.add(
                        Chapter(
                            audiobookId = 0L,
                            title = chTitle,
                            startTime = cumulativeTimeMs,
                            audioPath = audioFile?.absolutePath
                        )
                    )
                    cumulativeTimeMs += fileDuration
                }

                val coverPath = extractCoverImage(zf, zipFile.nameWithoutExtension, manifestCoverRelPath)

                val audiobook = Audiobook(
                    title = title,
                    author = author,
                    narrator = narrator,
                    filePath = zipFile.absolutePath,
                    coverUri = coverPath,
                    description = description,
                    duration = cumulativeTimeMs,
                    currentPosition = 0L,
                    lastPlayed = System.currentTimeMillis(),
                    parentSeries = parentSeries,
                    series = series,
                    seriesOrder = seriesOrder,
                    isFavorite = false
                )

                val bookId = repository.saveAudiobook(audiobook)
                val updatedChapters = chapters.map { it.copy(audiobookId = bookId) }
                repository.saveChapters(updatedChapters)

                audiobook.copy(id = bookId)

            }
        } catch (e: Exception) {
            Log.e("AbookStorage", "Fehler beim Importieren von ${zipFile.name}: ${e.message}", e)
            null
        }
    }

    private fun isAudioFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".m4b") || lower.endsWith(".ogg") || lower.endsWith(".flac") || lower.endsWith(".wav")
    }

    private fun extractCoverImage(zf: ZipFile, baseName: String, manifestCoverRelPath: String?): String? {
        val entries = zf.entries().asSequence().toList()
        
        // Versuche zuerst das im Manifest angegebene Cover zu finden
        var coverEntry: ZipEntry? = null
        if (!manifestCoverRelPath.isNullOrBlank()) {
            coverEntry = entries.firstOrNull {
                it.name.equals(manifestCoverRelPath, ignoreCase = true) ||
                File(it.name).name.equals(File(manifestCoverRelPath).name, ignoreCase = true)
            }
        }
        
        // Fallback: Automatische Suche nach cover.*, folder.* oder bildern
        if (coverEntry == null) {
            coverEntry = entries.firstOrNull {
                val fileName = File(it.name).name.lowercase()
                fileName.startsWith("cover.") || fileName.startsWith("folder.") ||
                fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg")
            }
        }
        if (coverEntry == null) return null

        val coversDir = LibraryLocationManager.getCoversDir(context)
        val ext = File(coverEntry.name).extension.ifBlank { "jpg" }
        val targetCoverFile = safeTargetFile(coversDir, "cover_${baseName.hashCode()}.$ext") ?: return null

        zf.getInputStream(coverEntry).use { input ->
            copyStreamSafely(input, targetCoverFile)
        }
        return targetCoverFile.absolutePath
    }

    /**
     * Zip-Slip-Schutz: stellt sicher, dass der aufgelöste Zielpfad tatsächlich
     * innerhalb von [baseDir] liegt, bevor eine Datei aus dem Archiv geschrieben wird.
     */
    fun safeTargetFile(baseDir: File, entryFileName: String): File? {
        val canonicalBase = baseDir.canonicalFile
        val candidate = File(canonicalBase, entryFileName).canonicalFile
        return if (candidate.path.startsWith(canonicalBase.path + File.separator)) candidate else null
    }

    private fun validateZipEntryRatio(entry: ZipEntry): Boolean {
        if (entry.compressedSize > 0 && entry.size > 0) {
            val ratio = entry.size / entry.compressedSize
            if (ratio > MAX_COMPRESSION_RATIO) {
                return false
            }
        }
        return true
    }

    private fun copyStreamSafely(input: InputStream, targetFile: File, maxSizeBytes: Long = MAX_SINGLE_FILE_SIZE_BYTES): Boolean {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        var bytesWritten = 0L
        try {
            FileOutputStream(tempFile).use { output ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    bytesWritten += read
                    if (bytesWritten > maxSizeBytes) {
                        Log.e("AbookStorage", "Datei überschreitet maximale Einzeldateigröße ($maxSizeBytes Bytes)")
                        if (tempFile.exists()) tempFile.delete()
                        return false
                    }
                    output.write(buffer, 0, read)
                }
            }
            if (tempFile.exists()) {
                if (targetFile.exists()) targetFile.delete()
                return tempFile.renameTo(targetFile)
            }
            return false
        } catch (e: Exception) {
            Log.e("AbookStorage", "Fehler bei atomarer Extraktion: ${e.message}", e)
            if (tempFile.exists()) tempFile.delete()
            return false
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex("_display_name")
                    if (columnIndex != -1) {
                        name = cursor.getString(columnIndex)
                    }
                }
            }
        }
        return name
    }

    suspend fun migrateLibraryToPublicFolder(context: Context, targetPublicDir: File): Int = withContext(Dispatchers.IO) {
        var migratedCount = 0
        try {
            if (!targetPublicDir.exists()) targetPublicDir.mkdirs()
            val internalFilesDir = context.filesDir
            val internalFilesPath = internalFilesDir.absolutePath
            val internalCanonicalPath = try { internalFilesDir.canonicalPath } catch (_: Exception) { internalFilesPath }

            fun isInternalPath(path: String): Boolean {
                if (path.isBlank()) return false
                val f = File(path)
                val abs = f.absolutePath
                val can = try { f.canonicalPath } catch (_: Exception) { abs }
                return abs.startsWith(internalFilesPath) || can.startsWith(internalCanonicalPath) ||
                        abs.contains("/de.f_soft_studio.abookplayer/files") ||
                        can.contains("/de.f_soft_studio.abookplayer/files")
            }

            fun getRelativePathFromInternal(file: File): String {
                val abs = file.absolutePath
                if (abs.startsWith(internalFilesPath)) return abs.removePrefix(internalFilesPath).removePrefix(File.separator)
                if (abs.startsWith(internalCanonicalPath)) return abs.removePrefix(internalCanonicalPath).removePrefix(File.separator)
                val idx = abs.indexOf("/de.f_soft_studio.abookplayer/files/")
                if (idx != -1) return abs.substring(idx + "/de.f_soft_studio.abookplayer/files/".length)
                return file.name
            }

            val allBooks = repository.getAllAudiobooks().first()
            for (book in allBooks) {
                var updatedBook = book
                var bookMigrated = false

                // 1. Haupt-Dateipfad prüfen / migrieren
                val bookPath = book.filePath
                if (bookPath.startsWith("content://")) {
                    val fileName = getFileNameFromUri(Uri.parse(bookPath)) ?: "imported_book_${book.id}"
                    val targetFile = File(targetPublicDir, fileName)
                    try {
                        context.contentResolver.openInputStream(Uri.parse(bookPath))?.use { input ->
                            copyStreamSafely(input, targetFile)
                        }
                        if (targetFile.exists() && targetFile.length() > 0L) {
                            updatedBook = updatedBook.copy(filePath = targetFile.absolutePath)
                            bookMigrated = true
                        }
                    } catch (e: Exception) {
                        Log.e("AbookStorage", "Fehler beim Kopieren von SAF-Uri $bookPath: ${e.message}")
                    }
                } else if (isInternalPath(bookPath)) {
                    val oldBookFile = File(bookPath)
                    if (oldBookFile.exists()) {
                        val relPath = getRelativePathFromInternal(oldBookFile)
                        val targetFile = File(targetPublicDir, relPath)
                        targetFile.parentFile?.mkdirs()

                        if (oldBookFile.isDirectory) {
                            oldBookFile.copyRecursively(targetFile, overwrite = true)
                        } else if (oldBookFile.isFile) {
                            oldBookFile.copyTo(targetFile, overwrite = true)
                        }

                        updatedBook = updatedBook.copy(filePath = targetFile.absolutePath)
                        bookMigrated = true
                    }
                }

                // 2. Cover-Uri prüfen / migrieren
                val coverUri = book.coverUri
                if (!coverUri.isNullOrBlank() && isInternalPath(coverUri)) {
                    val oldCoverFile = File(coverUri)
                    if (oldCoverFile.exists()) {
                        val relCoverPath = getRelativePathFromInternal(oldCoverFile)
                        val targetCoverFile = File(targetPublicDir, relCoverPath)
                        targetCoverFile.parentFile?.mkdirs()
                        oldCoverFile.copyTo(targetCoverFile, overwrite = true)
                        updatedBook = updatedBook.copy(coverUri = targetCoverFile.absolutePath)
                        bookMigrated = true
                    }
                }

                // 3. Kapitel-Audiopfade prüfen / migrieren
                val chapters = repository.getChaptersForAudiobook(book.id).first()
                val updatedChapters = mutableListOf<Chapter>()
                var chaptersChanged = false

                for (ch in chapters) {
                    val audioPath = ch.audioPath
                    if (!audioPath.isNullOrBlank()) {
                        if (audioPath.startsWith("content://")) {
                            val fileName = getFileNameFromUri(Uri.parse(audioPath)) ?: "track_${ch.id}.mp3"
                            val targetAudioFile = File(targetPublicDir, "audio_${book.id}/$fileName")
                            targetAudioFile.parentFile?.mkdirs()
                            try {
                                context.contentResolver.openInputStream(Uri.parse(audioPath))?.use { input ->
                                    copyStreamSafely(input, targetAudioFile)
                                }
                                if (targetAudioFile.exists()) {
                                    updatedChapters.add(ch.copy(audioPath = targetAudioFile.absolutePath))
                                    chaptersChanged = true
                                    bookMigrated = true
                                } else {
                                    updatedChapters.add(ch)
                                }
                            } catch (_: Exception) {
                                updatedChapters.add(ch)
                            }
                        } else if (isInternalPath(audioPath)) {
                            val oldAudioFile = File(audioPath)
                            if (oldAudioFile.exists()) {
                                val relAudioPath = getRelativePathFromInternal(oldAudioFile)
                                val targetAudioFile = File(targetPublicDir, relAudioPath)
                                targetAudioFile.parentFile?.mkdirs()
                                if (!targetAudioFile.exists() || targetAudioFile.length() != oldAudioFile.length()) {
                                    oldAudioFile.copyTo(targetAudioFile, overwrite = true)
                                }
                                updatedChapters.add(ch.copy(audioPath = targetAudioFile.absolutePath))
                                chaptersChanged = true
                                bookMigrated = true
                            } else {
                                updatedChapters.add(ch)
                            }
                        } else {
                            updatedChapters.add(ch)
                        }
                    } else {
                        updatedChapters.add(ch)
                    }
                }

                if (bookMigrated) {
                    repository.saveAudiobook(updatedBook)
                    migratedCount++
                }
                if (chaptersChanged) {
                    repository.saveChapters(updatedChapters)
                }
            }

            // 4. Aufräumen alter interner Dateien im privaten Speicher
            val subDirsToClean = listOf("audiobooks", "covers")
            for (dirName in subDirsToClean) {
                val internalFolder = File(internalFilesDir, dirName)
                if (internalFolder.exists()) internalFolder.deleteRecursively()
            }
            internalFilesDir.listFiles()?.forEach { f ->
                if (f.isDirectory && (f.name.startsWith("imported_") || f.name.startsWith("unpacked_"))) {
                    f.deleteRecursively()
                }
            }
        } catch (e: Exception) {
            Log.e("AbookStorage", "Fehler bei Bibliotheks-Migration: ${e.message}", e)
        }
        return@withContext migratedCount
    }
}
