package de.f_soft_studio.abookplayer.domain.model

/**
 * Domain-Modell für ein Kapitel eines Hörbuchs.
 *
 * Repräsentiert einen logischen oder dateibasierten Abschnitt eines Hörbuchs.
 *
 * @property id Eindeutige ID des Kapitels (0 für neue, noch nicht persistierte Kapitel).
 * @property audiobookId Fremdschlüssel-Referenz auf das zugehörige Hörbuch ([Audiobook.id]).
 * @property title Anzeigetitel des Kapitels (z. B. "Kapitel 1" oder "Prolog").
 * @property startTime Startzeitpunkt relativ zum Gesamt-Hörbuch in Millisekunden.
 * @property audioPath Pfad oder URI zur spezifischen Audiodatei (relevant bei dateibasierten/multi-file Kapiteln).
 */
data class Chapter(
    val id: Long = 0,
    val audiobookId: Long,
    val title: String,
    val startTime: Long,
    val audioPath: String? = null
)
