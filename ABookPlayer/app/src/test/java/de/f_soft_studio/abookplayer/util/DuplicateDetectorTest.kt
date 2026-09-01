package de.f_soft_studio.abookplayer.util

import de.f_soft_studio.abookplayer.domain.model.Audiobook
import org.junit.Assert.assertEquals
import org.junit.Test

class DuplicateDetectorTest {

    private fun book(title: String, author: String = "Autor", duration: Long = 0L, narrator: String? = null) =
        Audiobook(title = title, author = author, filePath = "/x/$title", duration = duration, narrator = narrator)

    @Test
    fun andererTitel_istUnique() {
        val r = DuplicateDetector.compare(
            newTitle = "Buch A", newAuthor = "Autor", newDurationMs = 1000L,
            newChapterCount = 3, newNarrator = null,
            existingBook = book("Buch B", duration = 1000L), existingChapterCount = 3
        )
        assertEquals(ComparisonType.UNIQUE, r)
    }

    @Test
    fun gleicheDauer_gleicherTitel_istIdentischesDuplikat() {
        val r = DuplicateDetector.compare(
            newTitle = "Der Hobbit", newAuthor = "Tolkien", newDurationMs = 3_600_000L,
            newChapterCount = 10, newNarrator = null,
            existingBook = book("Der Hobbit", "Tolkien", duration = 3_600_000L), existingChapterCount = 10
        )
        assertEquals(ComparisonType.IDENTICAL_DUPLICATE, r)
    }

    @Test
    fun deutlicheDauerabweichung_beideEchteDauer_istDifferentVersion() {
        val r = DuplicateDetector.compare(
            newTitle = "Der Hobbit", newAuthor = "Tolkien", newDurationMs = 3_600_000L,
            newChapterCount = 10, newNarrator = null,
            existingBook = book("Der Hobbit", "Tolkien", duration = 7_200_000L), existingChapterCount = 10
        )
        assertEquals(ComparisonType.DIFFERENT_VERSION, r)
    }

    @Test
    fun neueDauerUnbekannt_wirdNichtAlsAndereVersionGewertet() {
        // Kernfall des "(Edition)"-Klon-Bugs: erneut gescanntes kaputtes Hörbuch (Dauer 0)
        // gegen die gesunde Kopie mit echter Dauer -> muss IDENTICAL_DUPLICATE sein.
        val r = DuplicateDetector.compare(
            newTitle = "Genvater (ungekürzt)", newAuthor = "Guy Haley", newDurationMs = 0L,
            newChapterCount = 0, newNarrator = null,
            existingBook = book("Genvater (ungekürzt)", "Guy Haley", duration = 45_158_316L),
            existingChapterCount = 241
        )
        assertEquals(ComparisonType.IDENTICAL_DUPLICATE, r)
    }

    @Test
    fun bestehendeDauerUnbekannt_wirdNichtAlsAndereVersionGewertet() {
        val r = DuplicateDetector.compare(
            newTitle = "Genvater (ungekürzt)", newAuthor = "Guy Haley", newDurationMs = 45_158_316L,
            newChapterCount = 241, newNarrator = null,
            existingBook = book("Genvater (ungekürzt)", "Guy Haley", duration = 0L),
            existingChapterCount = 0
        )
        assertEquals(ComparisonType.IDENTICAL_DUPLICATE, r)
    }

    @Test
    fun andererSprecher_beiGleicherDauer_istDifferentVersion() {
        val r = DuplicateDetector.compare(
            newTitle = "Der Hobbit", newAuthor = "Tolkien", newDurationMs = 3_600_000L,
            newChapterCount = 10, newNarrator = "Sprecher A",
            existingBook = book("Der Hobbit", "Tolkien", duration = 3_600_000L, narrator = "Sprecher B"),
            existingChapterCount = 10
        )
        assertEquals(ComparisonType.DIFFERENT_VERSION, r)
    }
}
