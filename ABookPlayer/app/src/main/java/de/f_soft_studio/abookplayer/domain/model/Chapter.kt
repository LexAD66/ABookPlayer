package de.f_soft_studio.abookplayer.domain.model

/**
 * Domain-Modell für ein Kapitel eines Hörbuchs.
 *
 * @property id Eindeutige ID des Kapitels.
 * @property audiobookId Referenz auf die Hörbuch-ID.
 * @property title Titel des Kapitels.
 * @property startTime Startzeitpunkt im Hörbuch in Millisekunden.
 */
data class Chapter(
    val id: Long = 0,
    val audiobookId: Long,
    val title: String,
    val startTime: Long,
    val audioPath: String? = null
)
