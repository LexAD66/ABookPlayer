package de.f_soft_studio.abookplayer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AudiobookMetadataTextTest {

    @Test
    fun stripAudioExtension_entferntBekannteEndungen() {
        assertEquals("116 – Kapitel 116", AudiobookMetadataText.stripAudioExtension("116 – Kapitel 116.mp3"))
        assertEquals("Track 01", AudiobookMetadataText.stripAudioExtension("Track 01.M4B"))
        assertEquals("Intro", AudiobookMetadataText.stripAudioExtension("Intro.flac"))
    }

    @Test
    fun stripAudioExtension_laesstAnderePunkteUnberuehrt() {
        assertEquals("Kap. 3 – Der Wald", AudiobookMetadataText.stripAudioExtension("Kap. 3 – Der Wald"))
        assertEquals("Doku.txt", AudiobookMetadataText.stripAudioExtension("Doku.txt"))
    }

    @Test
    fun derive_dateinamensKapitelWirdBereinigt_buchtitelBleibtTitel() {
        val texts = AudiobookMetadataText.derive(
            bookTitle = "Die Säulen der Erde",
            author = "Ken Follett",
            chapterTitle = "116 – Kapitel 116.mp3"
        )
        assertEquals("116 – Kapitel 116", texts.title)
        assertEquals("Die Säulen der Erde • Ken Follett", texts.subtitle)
        assertEquals("Ken Follett", texts.artist)
    }

    @Test
    fun derive_leererBuchtitel_faelltNieAufRohdateinameZurueck() {
        val texts = AudiobookMetadataText.derive(
            bookTitle = "   ",
            author = "",
            chapterTitle = "116 – Kapitel 116.mp3"
        )
        assertEquals("116 – Kapitel 116", texts.title)
        assertNull(texts.subtitle)
        assertNull(texts.artist)
    }

    @Test
    fun derive_allesLeer_ergibtAppName() {
        val texts = AudiobookMetadataText.derive(bookTitle = "", author = "", chapterTitle = null)
        assertEquals("ABook Player", texts.title)
        assertNull(texts.subtitle)
        assertNull(texts.artist)
    }

    @Test
    fun derive_ohneKapitel_nutztBuchtitelUndAutor() {
        val texts = AudiobookMetadataText.derive(
            bookTitle = "Der Hobbit",
            author = "J. R. R. Tolkien",
            chapterTitle = null
        )
        assertEquals("Der Hobbit", texts.title)
        assertEquals("J. R. R. Tolkien", texts.subtitle)
        assertEquals("J. R. R. Tolkien", texts.artist)
    }

    @Test
    fun derive_kapitelGleichBuchtitel_wirdNichtDoppeltAngezeigt() {
        val texts = AudiobookMetadataText.derive(
            bookTitle = "Der Hobbit",
            author = "",
            chapterTitle = "Der Hobbit"
        )
        assertEquals("Der Hobbit", texts.title)
        assertNull(texts.subtitle)
        assertEquals("Der Hobbit", texts.artist)
    }

    @Test
    fun derive_kapitelOhneAutor_zeigtBuchtitelAlsUntertitel() {
        val texts = AudiobookMetadataText.derive(
            bookTitle = "Der Hobbit",
            author = "",
            chapterTitle = "Kapitel 1 – Eine unerwartete Gesellschaft"
        )
        assertEquals("Kapitel 1 – Eine unerwartete Gesellschaft", texts.title)
        assertEquals("Der Hobbit", texts.subtitle)
        assertEquals("Der Hobbit", texts.artist)
    }
}
