package de.f_soft_studio.abookplayer.domain.model

/**
 * Domain-Modell für eine Buchfigur / Charakter im Personenregister eines Hörbuchs.
 */
data class BookCharacter(
    val id: Long = 0,
    val audiobookId: Long,
    val name: String,
    val role: String = "",
    val description: String = "",
    val relationship: String = "",
    val isPrimary: Boolean = false
)
