
package de.f_soft_studio.abookplayer.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

/**
 * Hilfsfunktionen zur Ermittlung der Dauer von Audio-Dateien (MP3, M4B, M4A, FLAC, WAV, etc.)
 * sowohl für lokale Dateipfade als auch für SAF Content-URIs.
 */
object ChapterDurations {

    fun readDurationMs(context: Context?, pathOrUri: String): Long {
        if (pathOrUri.isBlank()) return 0L
        val mmr = MediaMetadataRetriever()
        return try {
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
    }

    fun readDurationMs(file: File): Long {
        if (!file.exists()) return 0L
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(file.absolutePath)
            val durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durStr?.toLongOrNull() ?: 0L
        } catch (_: Throwable) {
            0L
        } finally {
            try { mmr.release() } catch (_: Throwable) {}
        }
    }

    fun sumDuration(files: List<File>): Long = files.sumOf { readDurationMs(it) }
}
