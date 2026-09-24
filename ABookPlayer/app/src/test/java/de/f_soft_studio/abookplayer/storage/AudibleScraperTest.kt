package de.f_soft_studio.abookplayer.storage

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class AudibleScraperTest {

    private val context: Context = mockk(relaxed = true)
    private val scraper = OnlineCoverScraper(context)

    @Test
    fun `cleanHtml strips html tags and decodes common entities`() {
        val rawHtml = "<p>Dies ist ein <b>Test</b>.<br/>Zweite Zeile mit &quot;Zitaten&quot; &amp; &lt;Tags&gt;.</p>"
        val cleaned = scraper.cleanHtml(rawHtml)
        assertEquals("Dies ist ein Test.\nZweite Zeile mit \"Zitaten\" & <Tags>.", cleaned)
    }

    @Test
    fun `cleanQueryText removes file extensions, part indicators and unabridged tags`() {
        val raw = "Dan Abnett - Warhammer 40.000 - Sabbat Worlds Crusade 01 - Der Doppelte Adler (ungekürzt).m4b"
        val cleaned = scraper.cleanQueryText(raw)
        assertEquals("Dan Abnett - Warhammer 40.000 - Sabbat Worlds Crusade 01 - Der Doppelte Adler", cleaned)

        val dotRaw = "Gav.Thorpe.-.Warhammer.40.000.-.Space.Marine.Conquests.02.-.Die.Trümmer.von.Prospero.ungekürzt"
        val dotCleaned = scraper.cleanQueryText(dotRaw)
        assertEquals("Gav Thorpe - Warhammer 40.000 - Space Marine Conquests 02 - Die Trümmer von Prospero", dotCleaned)
    }

    @Test
    fun `parseAudibleJson extracts all fields correctly`() {
        val sampleJson = """
            {
              "products": [
                {
                  "asin": "B004UZKBW8",
                  "title": "Eragon - Der Auftrag des Ältesten",
                  "subtitle": "Eragon 2",
                  "authors": [
                    { "name": "Christopher Paolini" },
                    { "name": "Joannis Stefanidis - Übersetzer" }
                  ],
                  "narrators": [
                    { "name": "Andreas Fröhlich" }
                  ],
                  "series": [
                    { "title": "Eragon", "sequence": "2" }
                  ],
                  "merchandising_summary": "<p>Geschunden, aber siegreich war Eragon...</p>",
                  "publisher_name": "Random House Audio",
                  "release_date": "2005-12-23",
                  "product_images": {
                    "500": "https://m.media-amazon.com/images/I/51JzBkOxQWL._SL500_.jpg"
                  }
                }
              ]
            }
        """.trimIndent()

        val result = scraper.parseAudibleJson(sampleJson, "Fallback Titel", "Fallback Autor", downloadCover = false)
        assertNotNull(result)
        assertEquals("Eragon - Der Auftrag des Ältesten", result?.title)
        assertEquals("Christopher Paolini, Joannis Stefanidis - Übersetzer", result?.author)
        assertEquals("Andreas Fröhlich", result?.narrator)
        assertEquals("Eragon", result?.series)
        assertEquals(2, result?.seriesOrder)
        assertEquals("Geschunden, aber siegreich war Eragon...", result?.description)
        assertEquals("Random House Audio", result?.publisher)
        assertEquals("2005-12-23", result?.releaseDate)
        assertEquals("Audible (DE)", result?.providerName)
    }

    @Test
    fun `parseAudibleProductsJson extracts multiple products correctly`() {
        val multiJson = """
            {
              "products": [
                {
                  "asin": "B01",
                  "title": "Die Trümmer von Prospero",
                  "authors": [{ "name": "Gav Thorpe" }],
                  "narrators": [{ "name": "David Nathan" }],
                  "series": [{ "title": "Space Marine Conquests", "sequence": "2" }],
                  "product_images": { "500": "https://m.media-amazon.com/images/I/51A._SL500_.jpg" }
                },
                {
                  "asin": "B02",
                  "title": "Magnus: Der Herr von Prospero",
                  "authors": [{ "name": "Graham McNeill" }],
                  "narrators": [{ "name": "Dietmar Wunder" }],
                  "series": [{ "title": "Die Primarchen", "sequence": "3" }],
                  "product_images": { "500": "https://m.media-amazon.com/images/I/51B._SL500_.jpg" }
                }
              ]
            }
        """.trimIndent()

        val results = scraper.parseAudibleProductsJson(multiJson)
        assertEquals(2, results.size)

        val first = results[0]
        assertEquals("Die Trümmer von Prospero", first.title)
        assertEquals("Gav Thorpe", first.author)
        assertEquals("David Nathan", first.narrator)
        assertEquals("Space Marine Conquests", first.series)
        assertEquals(2, first.seriesOrder)
        assertEquals("https://m.media-amazon.com/images/I/51A._SL1000_.jpg", first.coverUrl)

        val second = results[1]
        assertEquals("Magnus: Der Herr von Prospero", second.title)
        assertEquals("Graham McNeill", second.author)
        assertEquals("Dietmar Wunder", second.narrator)
        assertEquals("Die Primarchen", second.series)
        assertEquals(3, second.seriesOrder)
        assertEquals("https://m.media-amazon.com/images/I/51B._SL1000_.jpg", second.coverUrl)
    }

    @Test
    fun `parseITunesProductsJson extracts audiobooks and upgrades cover to 600x600`() {
        val itunesJson = """
            {
              "resultCount": 1,
              "results": [
                {
                  "trackName": "Magnus der Rote",
                  "artistName": "Graham McNeill",
                  "artworkUrl100": "https://is1-ssl.mzstatic.com/image/thumb/Music/v4/100x100bb.jpg",
                  "description": "Ein spannendes Hörbuch."
                }
              ]
            }
        """.trimIndent()

        val results = scraper.parseITunesProductsJson(itunesJson)
        assertEquals(1, results.size)
        val item = results.first()
        assertEquals("Magnus der Rote", item.title)
        assertEquals("Graham McNeill", item.author)
        assertEquals("https://is1-ssl.mzstatic.com/image/thumb/Music/v4/600x600bb.jpg", item.coverUrl)
        assertEquals("iTunes", item.providerName)
    }

    @Test
    fun `parseAudibleJson with empty products returns null`() {
        val emptyJson = """{ "products": [] }"""
        val result = scraper.parseAudibleJson(emptyJson, "Titel", "Autor", downloadCover = false)
        assertNull(result)
    }

    @Test
    fun `parseAudibleJson with invalid json returns null without crash`() {
        val invalidJson = """not a json"""
        val result = scraper.parseAudibleJson(invalidJson, "Titel", "Autor", downloadCover = false)
        assertNull(result)
    }
}
