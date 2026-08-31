package de.f_soft_studio.abookplayer.domain.model

/**
 * Domain-Modell für ein Hörbuch.
 *
 * @property id Eindeutige ID des Hörbuchs.
 * @property title Titel des Hörbuchs.
 * @property author Autor des Hörbuchs.
 * @property narrator Sprecher des Hörbuchs.
 * @property filePath Pfad zur Audiodatei oder zum Verzeichnis.
 * @property coverUri URI des Cover-Bildes.
 * @property description Beschreibung des Hörbuchs.
 * @property duration Gesamtdauer in Millisekunden.
 * @property currentPosition Aktuelle Wiedergabeposition in Millisekunden.
 * @property lastPlayed Zeitstempel der letzten Wiedergabe.
 * @property addedAt Zeitstempel des Imports (für Sortierung „Neu importiert").
 */
data class Audiobook(
    val id: Long = 0,
    val title: String,
    val author: String = "",
    val narrator: String? = null,
    val filePath: String,
    val coverUri: String? = null,
    val description: String? = null,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val lastPlayed: Long = System.currentTimeMillis(),
    val addedAt: Long = System.currentTimeMillis(),
    val parentSeries: String? = null,
    val series: String? = null,
    val seriesOrder: Int? = null,
    val isFavorite: Boolean = false,
    val customSpeed: Float? = null,
    val equalizerPreset: String? = null
)

