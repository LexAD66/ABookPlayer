package de.f_soft_studio.abookplayer.util

import java.io.File

/**
 * Entscheidet, welche Pfade tatsächlich abspielbare Audiodateien sind.
 *
 * Hintergrund: `File.exists()` liefert auch für Verzeichnisse `true`. Wird ein
 * Verzeichnis an ExoPlayer übergeben (z. B. ein `imported_*`-Ordner, dessen
 * Dateien verschoben oder gelöscht wurden), bricht die Wiedergabe mit
 * `EISDIR (Is a directory)` ab – der Player wirkt dann „reagiert nicht", ohne
 * dass die UI einen Fehler zeigt. Nur reguläre Dateien sind abspielbar.
 */
object PlayableMedia {

    /** true nur, wenn [path] auf eine existierende reguläre Datei zeigt (kein Verzeichnis, nicht leer). */
    fun isPlayableFile(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return File(path).isFile
    }

    /** Filtert eine (Kapitel-)Pfadliste auf tatsächlich abspielbare Dateien; Reihenfolge bleibt erhalten. */
    fun playableFiles(paths: List<String?>): List<File> =
        paths.filter { isPlayableFile(it) }.map { File(it!!) }
}
