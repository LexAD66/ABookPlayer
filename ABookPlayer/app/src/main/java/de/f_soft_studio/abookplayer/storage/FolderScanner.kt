package de.f_soft_studio.abookplayer.storage

import android.content.Context
import android.util.Log
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Chapter
import de.f_soft_studio.abookplayer.util.AbookModels
import de.f_soft_studio.abookplayer.util.ChapterDurations
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

data class ScannedAudiobookResult(
    val audiobook: Audiobook,
    val chapters: List<Chapter>
)

/**
 * Scanner für ungepackte Hörbuchordner.
 * Erkennt Unterordner mit Audiodatei-Inhalt als Hörbücher und führt
 * mehrteilige CD/Disc-Unterordner (z. B. CD1, CD2, Disc 1, Disc 2) zu EINEM Hörbuch zusammen.
 */
class FolderScanner(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    companion object {
        private val AUDIO_EXTENSIONS = setOf("mp3", "m4a", "m4b", "ogg", "flac", "wav")
        private val DISC_FOLDER_REGEX = Regex("""(?i)^(cd|disc|disk|teil|part)\s*[-_]?\s*\d+.*""")

        fun isAudioFile(file: File): Boolean {
            return file.isFile && AUDIO_EXTENSIONS.contains(file.extension.lowercase())
        }

        fun isDiscSubfolder(folder: File): Boolean {
            return folder.isDirectory && DISC_FOLDER_REGEX.matches(folder.name.trim())
        }
    }

    /**
     * Scannt ein Verzeichnis nach Hörbuchordnern.
     */
    fun scanDirectory(baseDir: File): List<ScannedAudiobookResult> {
        if (!baseDir.exists() || !baseDir.isDirectory) return emptyList()

        val results = mutableListOf<ScannedAudiobookResult>()
        val subDirs = baseDir.listFiles { f -> f.isDirectory } ?: emptyArray()

        for (dir in subDirs) {
            val scanned = scanBookFolder(dir)
            if (scanned != null) {
                results.add(scanned)
            }
        }
        return results
    }

    /**
     * Verarbeitet einen einzelnen Buchordner (z. B. "Der Herr der Ringe").
     * Prüft, ob Unterordner wie CD1, CD2 vorliegen und fasst alle Audio-Dateien zusammen.
     */
    fun scanBookFolder(bookDir: File): ScannedAudiobookResult? {
        if (!bookDir.exists() || !bookDir.isDirectory) return null

        val allAudioFiles = mutableListOf<File>()

        val directSubFiles = bookDir.listFiles() ?: emptyArray()
        val directAudioFiles = directSubFiles.filter { isAudioFile(it) }.sortedBy { it.name }

        val discSubfolders = directSubFiles.filter { isDiscSubfolder(it) }
            .sortedWith(Comparator { f1, f2 -> naturalCompare(f1.name, f2.name) })

        val otherSubfolders = directSubFiles.filter { it.isDirectory && !isDiscSubfolder(it) }
            .sortedWith(Comparator { f1, f2 -> naturalCompare(f1.name, f2.name) })

        if (discSubfolders.isNotEmpty()) {
            for (discDir in discSubfolders) {
                val discAudioFiles = (discDir.listFiles() ?: emptyArray())
                    .filter { isAudioFile(it) }
                    .sortedWith(Comparator { f1, f2 -> naturalCompare(f1.name, f2.name) })
                allAudioFiles.addAll(discAudioFiles)
            }
            if (directAudioFiles.isNotEmpty()) {
                allAudioFiles.addAll(directAudioFiles)
            }
        } else if (directAudioFiles.isNotEmpty()) {
            allAudioFiles.addAll(directAudioFiles)
            for (subDir in otherSubfolders) {
                val subAudioFiles = (subDir.listFiles() ?: emptyArray())
                    .filter { isAudioFile(it) }
                    .sortedWith(Comparator { f1, f2 -> naturalCompare(f1.name, f2.name) })
                allAudioFiles.addAll(subAudioFiles)
            }
        } else if (otherSubfolders.isNotEmpty()) {
            for (subDir in otherSubfolders) {
                val subAudioFiles = (subDir.listFiles() ?: emptyArray())
                    .filter { isAudioFile(it) }
                    .sortedWith(Comparator { f1, f2 -> naturalCompare(f1.name, f2.name) })
                allAudioFiles.addAll(subAudioFiles)
            }
        }

        if (allAudioFiles.isEmpty()) return null

        var title = bookDir.name
        var author = ""
        var narrator: String? = null
        var description: String? = null
        var coverUri: String? = null

        val manifestFile = File(bookDir, "manifest.json")
        val metadataFile = File(bookDir, "metadata.json")

        if (manifestFile.exists()) {
            try {
                val text = manifestFile.readText()
                val manifest = json.decodeFromString<AbookModels.AbookManifestRaw>(text)
                title = manifest.title.ifBlank { title }
                author = manifest.author ?: ""
                narrator = manifest.narrator
                description = manifest.description
                if (!manifest.cover.isNullOrBlank()) {
                    val cFile = File(bookDir, manifest.cover)
                    if (cFile.exists()) coverUri = cFile.absolutePath
                }
            } catch (e: Exception) {
                Log.w("FolderScanner", "Fehler beim Lesen von manifest.json in ${bookDir.name}: ${e.message}")
            }
        } else if (metadataFile.exists()) {
            try {
                val text = metadataFile.readText()
                val meta = json.decodeFromString<AbookModels.AbookMetaRaw>(text)
                title = meta.title.ifBlank { title }
                author = meta.author ?: ""
                description = meta.description
            } catch (e: Exception) {
                Log.w("FolderScanner", "Fehler beim Lesen von metadata.json in ${bookDir.name}: ${e.message}")
            }
        }

        if (coverUri == null) {
            val imageFiles = directSubFiles.filter { f ->
                f.isFile && (f.extension.lowercase() in setOf("jpg", "jpeg", "png", "webp"))
            }
            val coverCandidate = imageFiles.firstOrNull { f ->
                val name = f.nameWithoutExtension.lowercase()
                name.startsWith("cover") || name.startsWith("folder") || name.startsWith("front")
            } ?: imageFiles.firstOrNull()
            coverUri = coverCandidate?.absolutePath
        }

        if (coverUri == null && allAudioFiles.isNotEmpty()) {
            try {
                val firstAudio = allAudioFiles.first()
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(firstAudio.absolutePath)
                val art = retriever.embeddedPicture
                retriever.release()
                if (art != null && art.isNotEmpty()) {
                    val coversDir = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
                    val targetFile = File(coversDir, "cover_embedded_${bookDir.name.hashCode()}.jpg")
                    targetFile.writeBytes(art)
                    coverUri = targetFile.absolutePath
                }
            } catch (_: Exception) {}
        }

        var cumulativeTimeMs = 0L
        val chapters = mutableListOf<Chapter>()

        for ((index, audioFile) in allAudioFiles.withIndex()) {
            val durationMs = ChapterDurations.readDurationMs(audioFile)
            val parentName = audioFile.parentFile?.name ?: ""
            val chTitle = if (isDiscSubfolder(audioFile.parentFile!!)) {
                "$parentName - ${audioFile.nameWithoutExtension}"
            } else {
                audioFile.nameWithoutExtension
            }

            chapters.add(
                Chapter(
                    audiobookId = 0L,
                    title = chTitle,
                    startTime = cumulativeTimeMs,
                    audioPath = audioFile.absolutePath
                )
            )
            cumulativeTimeMs += durationMs
        }

        val audiobook = Audiobook(
            id = 0L,
            title = title,
            author = author,
            narrator = narrator,
            filePath = bookDir.absolutePath,
            coverUri = coverUri,
            description = description,
            duration = cumulativeTimeMs,
            currentPosition = 0L,
            lastPlayed = System.currentTimeMillis()
        )

        return ScannedAudiobookResult(audiobook, chapters)
    }

    private fun naturalCompare(s1: String, s2: String): Int {
        val p1 = extractNumber(s1)
        val p2 = extractNumber(s2)
        return if (p1 != null && p2 != null) {
            val cmp = p1.first.compareTo(p2.first)
            if (cmp != 0) cmp else s1.compareTo(s2, ignoreCase = true)
        } else {
            s1.compareTo(s2, ignoreCase = true)
        }
    }

    private fun extractNumber(s: String): Pair<Int, String>? {
        val match = Regex("""\d+""").find(s) ?: return null
        return Pair(match.value.toIntOrNull() ?: 0, s)
    }
}
