
package de.f_soft_studio.abookplayer.util

import android.media.MediaMetadataRetriever
import java.io.File

/**
 * Hilfsfunktionen zur Ermittlung der Dauer von MP3-Dateien pro Kapitel.
 */
object ChapterDurations {

    fun readDurationMs(file: File): Long {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(file.absolutePath)
            val dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            dur ?: 0L
        } catch (_: Throwable) {
            0L
        } finally {
            try { mmr.release() } catch (_: Throwable) {}
        }
    }

    fun sumDuration(files: List<File>): Long = files.sumOf { readDurationMs(it) }
}
