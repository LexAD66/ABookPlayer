package de.f_soft_studio.abookplayer.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Chapter
import de.f_soft_studio.abookplayer.domain.model.ExportState
import de.f_soft_studio.abookplayer.util.AbookModels
import de.f_soft_studio.abookplayer.util.ChapterDurations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

import kotlinx.coroutines.flow.first

/**
 * AbookStorage: Scannt den Download-Ordner (/Download/ und /Download/ABook/) nach .abook-Dateien (ZIP),
 * unterstützt SAF-Uri-Import, verarbeitet kanonische manifest.json (Format v1) sowie legacy metadata.json,
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
    }

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val folderScanner = FolderScanner(context)

    suspend fun scanAndImport(): List<Audiobook> = withContext(Dispatchers.IO) {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val abookFolder = File(root, "ABook")
        if (!abookFolder.exists()) abookFolder.mkdirs()

        val filesToScan = mutableListOf<File>()
        root.listFiles { f -> f.isFile && f.extension.equals("abook", true) }?.let { filesToScan.addAll(it) }
        abookFolder.listFiles { f -> f.isFile && f.extension.equals("abook", true) }?.let { filesToScan.addAll(it) }

        val imported = mutableListOf<Audiobook>()
        val existingBooks = try { repository.getAllAudiobooks().first() } catch (_: Exception) { emptyList() }

        // 1. .abook Archive verarbeiten
        filesToScan.distinctBy { it.absolutePath }.forEach { zipFile ->
            // Duplikatprüfung anhand Pfad
            if (existingBooks.none { it.filePath == zipFile.absolutePath }) {
                val book = parseAndSaveAbook(zipFile)
                if (book != null) {
                    imported.add(book)
                }
            }
        }

        // 2. Ungepackte Hörbuchordner und CD1/CD2 Unterordner verarbeiten
        scanUnpackedFolderInternal(abookFolder, existingBooks, imported)
        scanUnpackedFolderInternal(root, existingBooks, imported)

        imported
    }

    private suspend fun scanUnpackedFolderInternal(
        baseDir: File,
        existingBooks: List<Audiobook>,
        importedList: MutableList<Audiobook>
    ) {
        if (!baseDir.exists() || !baseDir.isDirectory) return
        val scannedResults = folderScanner.scanDirectory(baseDir)
        for (result in scannedResults) {
            val book = result.audiobook
            val isDuplicate = existingBooks.any {
                it.filePath == book.filePath ||
                (it.title.equals(book.title, ignoreCase = true) && it.author.equals(book.author, ignoreCase = true))
            } || importedList.any {
                it.filePath == book.filePath ||
                (it.title.equals(book.title, ignoreCase = true) && it.author.equals(book.author, ignoreCase = true))
            }

            if (!isDuplicate) {
                val bookId = repository.saveAudiobook(book)
                val updatedChapters = result.chapters.map { it.copy(audiobookId = bookId) }
                repository.saveChapters(updatedChapters)
                importedList.add(book.copy(id = bookId))
            } else {
                Log.d("AbookStorage", "Duplikat ignoriert: ${book.title}")
            }
        }
    }

    /**
     * Importiert eine .abook-Datei über eine SAF-Uri (Storage Access Framework Picker) mit Persistierung der URI-Rechte.
     */
    suspend fun importFromUri(uri: Uri): Audiobook? = withContext(Dispatchers.IO) {
        return@withContext try {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                Log.d("AbookStorage", "Persistable URI permission nicht verfügbar oder bereits erteilt: ${e.message}")
            }

            val audiobooksDir = File(context.filesDir, "audiobooks").apply { if (!exists()) mkdirs() }
            val fileName = getFileNameFromUri(uri) ?: "imported_${System.currentTimeMillis()}.abook"
            val destFile = File(audiobooksDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                if (!copyStreamSafely(input, destFile, maxSizeBytes = MAX_TOTAL_UNCOMPRESSED_SIZE_BYTES)) {
                    Log.e("AbookStorage", "Fehler bei der Übertragung aus SAF-Uri")
                    return@withContext null
                }
            } ?: return@withContext null

            parseAndSaveAbook(destFile)
        } catch (e: Exception) {
            Log.e("AbookStorage", "Fehler beim SAF-Import aus Uri $uri: ${e.message}", e)
            null
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

                val unpackedDir = File(context.filesDir, "unpacked_${zipFile.nameWithoutExtension.hashCode()}").apply {
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
                    lastPlayed = System.currentTimeMillis()
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

        val coversDir = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
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
        if (name == null) {
            name = uri.path?.let { File(it).name }
        }
        return name
    }
}
