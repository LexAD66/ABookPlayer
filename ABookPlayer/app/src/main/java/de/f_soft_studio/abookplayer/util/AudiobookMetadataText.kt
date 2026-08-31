package de.f_soft_studio.abookplayer.util

/**
 * Reine Textlogik für die MediaSession-Metadaten eines Hörbuchs.
 *
 * Ohne Android-Framework-Abhängigkeiten, damit die Ableitung von Titel / Untertitel / Interpret
 * testbar ist. Verhindert insbesondere, dass in Android Auto oder auf dem Sperrbildschirm ein
 * roher Dateiname (z. B. "116 – Kapitel 116.mp3") als Titel erscheint, wenn Kapiteltitel eine
 * Datei-Endung tragen oder der Buchtitel leer ist.
 */
object AudiobookMetadataText {

    private val AUDIO_EXTENSIONS = setOf(
        "mp3", "m4a", "m4b", "ogg", "flac", "wav", "aac", "opus", "wma"
    )

    /**
     * Entfernt eine bekannte Audio-Datei-Endung am Ende von [name] (case-insensitive).
     * Andere Punkte im Namen (z. B. "Kap. 3") bleiben unangetastet.
     */
    fun stripAudioExtension(name: String): String {
        val trimmed = name.trim()
        val dot = trimmed.lastIndexOf('.')
        if (dot <= 0 || dot == trimmed.lastIndex) return trimmed
        val ext = trimmed.substring(dot + 1).lowercase()
        return if (ext in AUDIO_EXTENSIONS) trimmed.substring(0, dot).trim() else trimmed
    }

    data class Texts(
        val title: String,
        val subtitle: String?,
        val artist: String?,
    )

    /**
     * Leitet die anzuzeigenden Metadaten-Texte ab.
     *
     * - [Texts.title] ist nie leer: Kapitel (bereinigt) falls sinnvoll, sonst Buchtitel, sonst "ABook Player".
     * - [Texts.subtitle] kombiniert Buchtitel und Autor bzw. nur den Autor.
     * - [Texts.artist] ist der Autor, falls vorhanden, sonst der Buchtitel.
     */
    fun derive(bookTitle: String, author: String, chapterTitle: String?): Texts {
        val book = bookTitle.trim().ifBlank { null }
        val cleanedAuthor = author.trim().ifBlank { null }
        val chapter = chapterTitle
            ?.let { stripAudioExtension(it) }
            ?.ifBlank { null }
            ?.takeIf { it != book }

        val title = chapter ?: book ?: "ABook Player"

        val subtitle = when {
            chapter != null && book != null && cleanedAuthor != null -> "$book • $cleanedAuthor"
            chapter != null && book != null -> book
            cleanedAuthor != null -> cleanedAuthor
            else -> null
        }

        val artist = cleanedAuthor ?: book

        return Texts(title = title, subtitle = subtitle, artist = artist)
    }
}
