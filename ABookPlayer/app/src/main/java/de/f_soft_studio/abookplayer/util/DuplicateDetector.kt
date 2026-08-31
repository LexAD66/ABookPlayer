package de.f_soft_studio.abookplayer.util

import de.f_soft_studio.abookplayer.domain.model.Audiobook
import kotlin.math.abs

enum class ComparisonType {
    IDENTICAL_DUPLICATE,
    DIFFERENT_VERSION,
    UNIQUE
}

data class DuplicateMatch(
    val existingAudiobook: Audiobook,
    val candidateTitle: String,
    val candidatePath: String,
    val comparisonType: ComparisonType,
    val durationDifferenceMs: Long,
    val isSafUri: Boolean = false
)

object DuplicateDetector {

    /**
     * Vergleicht ein Kandidaten-Hörbuch mit einem bestehenden Hörbuch aus der Bibliothek.
     * Unterscheidet zwischen 100% Identischem Doppelgänger und Verschiedenen Versionen (z. B. gekürzt/ungekürzt oder anderer Sprecher).
     */
    fun compare(
        newTitle: String,
        newAuthor: String,
        newDurationMs: Long,
        newChapterCount: Int,
        newNarrator: String?,
        existingBook: Audiobook,
        existingChapterCount: Int
    ): ComparisonType {
        val titleMatch = normalizeText(newTitle).equals(normalizeText(existingBook.title), ignoreCase = true)
        if (!titleMatch) return ComparisonType.UNIQUE

        val authorMatch = newAuthor.isBlank() || existingBook.author.isBlank() ||
                normalizeText(newAuthor).equals(normalizeText(existingBook.author), ignoreCase = true)
        if (!authorMatch) return ComparisonType.UNIQUE

        val durationDiff = abs(newDurationMs - existingBook.duration)
        val sameDuration = durationDiff <= 5000L // Max 5 Sekunden Abweichung
        val sameChapters = newChapterCount == 0 || existingChapterCount == 0 || abs(newChapterCount - existingChapterCount) <= 1
        val sameNarrator = newNarrator.isNullOrBlank() || existingBook.narrator.isNullOrBlank() ||
                normalizeText(newNarrator).equals(normalizeText(existingBook.narrator ?: ""), ignoreCase = true)

        return if (sameDuration && sameChapters && sameNarrator) {
            ComparisonType.IDENTICAL_DUPLICATE
        } else {
            ComparisonType.DIFFERENT_VERSION
        }
    }

    fun normalizeText(text: String): String {
        return text.trim().lowercase()
            .replace(Regex("""[^a-z0-9äöüß\s]"""), "")
            .replace(Regex("""\s+"""), " ")
    }
}
