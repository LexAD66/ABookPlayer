package de.f_soft_studio.abookplayer.domain.model

/**
 * Zentrales Domain-Modell für ein Hörbuch im ABook Player.
 *
 * Repräsentiert ein Hörbuch unabhängig von Android-spezifischen Frameworks oder
 * Datenquellen (Room-Entity, JSON-Manifest oder MediaItem).
 *
 * @property id Eindeutige Datenbank-ID des Hörbuchs (0 bei noch nicht persistierten Objekten).
 * @property title Titel des Hörbuchs.
 * @property author Autor bzw. Verfasser des Werks.
 * @property narrator Sprecher oder Sprecherensemble des Hörbuchs.
 * @property filePath Dateisystempfad oder SAF-URI (kann auf eine `.abook`-Datei, Einzeldatei oder ein Verzeichnis verweisen).
 * @property coverUri Lokaler URI-Pfad zum Coverbild (in der Regel im App-internen Cover-Cache).
 * @property description Klappentext oder Zusammenfassung des Inhalts.
 * @property duration Gesamtlaufzeit aller Kapitel in Millisekunden.
 * @property currentPosition Zuletzt gemerkte Abspielposition in Millisekunden.
 * @property lastPlayed Zeitstempel (Epoche in ms) der letzten Wiedergabe für Chronologie & Sortierung.
 * @property addedAt Zeitstempel (Epoche in ms) des Imports für die Sortierung „Neu hinzugefügt".
 * @property parentSeries Übergeordnete Serie / Franchise (z. B. "Horus Heresy").
 * @property series Name der Buchreihe (z. B. "Die Primarchen").
 * @property seriesOrder Bandnummer innerhalb der Buchreihe.
 * @property isFavorite Markierung als Favorit zur schnellen Filterung in der Bibliothek.
 * @property customSpeed Hörbuch-spezifische Wiedergabegeschwindigkeit (null = globale Standardeinstellung).
 * @property equalizerPreset Hörbuch-spezifisches Equalizer-/Klangprofil (optional).
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

