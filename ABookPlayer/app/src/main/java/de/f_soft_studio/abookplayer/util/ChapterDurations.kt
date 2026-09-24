
package de.f_soft_studio.abookplayer.util

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

/**
 * Hilfsfunktionen zur Ermittlung der Dauer von Audio-Dateien (MP3, M4B, M4A, FLAC, WAV, etc.)
 * sowohl für lokale Dateipfade als auch für SAF Content-URIs mit robuster Fallback-Hierarchie.
 */
object ChapterDurations {

    fun readDurationMs(context: Context?, pathOrUri: String): Long {
        if (pathOrUri.isBlank()) return 0L

        // 1. Versuch: MediaMetadataRetriever
        val mmr = MediaMetadataRetriever()
        var duration = try {
            if (pathOrUri.startsWith("content://")) {
                if (context != null) {
                    mmr.setDataSource(context, Uri.parse(pathOrUri))
                } else return 0L
            } else {
                val file = File(pathOrUri)
                if (!file.exists()) return 0L
                mmr.setDataSource(file.absolutePath)
            }
            val durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durStr?.toLongOrNull() ?: 0L
        } catch (_: Throwable) {
            0L
        } finally {
            try { mmr.release() } catch (_: Throwable) {}
        }

        if (duration > 0L) return duration

        // 2. Versuch: MediaExtractor (Container-Header-Parsing)
        duration = try {
            val extractor = MediaExtractor()
            if (pathOrUri.startsWith("content://")) {
                if (context != null) {
                    extractor.setDataSource(context, Uri.parse(pathOrUri), null)
                } else null
            } else {
                extractor.setDataSource(pathOrUri)
            }
            var extDur = 0L
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true && format.containsKey(MediaFormat.KEY_DURATION)) {
                    val durUs = format.getLong(MediaFormat.KEY_DURATION)
                    if (durUs > 0) {
                        extDur = durUs / 1000L
                        break
                    }
                }
            }
            extractor.release()
            extDur
        } catch (_: Throwable) {
            0L
        }

        if (duration > 0L) return duration

        // 3. Versuch: Grobe Schätzung über Dateigröße für lokale Dateien (128 kbps Standard)
        if (!pathOrUri.startsWith("content://")) {
            val file = File(pathOrUri)
            if (file.exists() && file.length() > 0L) {
                val bytes = file.length()
                // Bei 128 kbps sind es ca. 16.000 Bytes pro Sekunde
                duration = (bytes * 1000L) / 16000L
            }
        }

        return duration
    }

    fun readDurationMs(file: File): Long {
        return readDurationMs(null, file.absolutePath)
    }

    fun sumDuration(files: List<File>): Long = files.sumOf { readDurationMs(it) }
}
