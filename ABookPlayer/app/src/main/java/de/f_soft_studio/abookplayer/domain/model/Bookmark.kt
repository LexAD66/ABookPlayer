package de.f_soft_studio.abookplayer.domain.model

/**
 * Domain-Modell für ein Lesezeichen (Bookmark).
 *
 * @property id Eindeutige ID des Lesezeichens.
 * @property audiobookId Referenz auf das Hörbuch.
 * @property position Wiedergabeposition in Millisekunden, an der das Lesezeichen gesetzt wurde.
 * @property note Optionale Notiz oder Beschreibung zum Lesezeichen.
 * @property createdAt Erstellungszeitpunkt als Timestamp.
 */
data class Bookmark(
    val id: Long = 0,
    val audiobookId: Long,
    val position: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
