package de.f_soft_studio.abookplayer.domain.model

/**
 * Domain-Modell für eine Buchfigur / einen Charakter im Personenregister eines Hörbuchs.
 *
 * Ermöglicht dem Hörer den Überblick über komplexe Figurenkonstellationen und Rollen.
 *
 * @property id Eindeutige ID des Eintrags.
 * @property audiobookId Fremdschlüssel auf das zugehörige Hörbuch ([Audiobook.id]).
 * @property name Name der Figur (z. B. "Magnus der Rote").
 * @property role Rolle der Figur (z. B. "Primarch der Thousand Sons").
 * @property description Detaillierte Charakterbeschreibung oder Hintergrundinfos.
 * @property relationship Beziehung zu anderen Figuren (z. B. "Bruder von Leman Russ").
 * @property isPrimary Gibt an, ob es sich um eine Hauptfigur handelt.
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
