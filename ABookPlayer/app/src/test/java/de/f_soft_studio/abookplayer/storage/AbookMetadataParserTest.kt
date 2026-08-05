package de.f_soft_studio.abookplayer.storage

import de.f_soft_studio.abookplayer.util.AbookModels
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit-Test zur Validierung der Deserialisierung von metadata.json aus .abook-Dateien.
 */
class AbookMetadataParserTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse metadata json with complete fields returns valid object`() {
        val sampleJson = """
            {
                "title": "Sherlock Holmes",
                "author": "Arthur Conan Doyle",
                "description": "Klassische Detektivgeschichte",
                "chapters": [
                    { "file": "01_intro.mp3", "title": "Kapitel 1: Einleitung" },
                    { "file": "02_case.mp3", "title": "Kapitel 2: Der Fall" }
                ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString<AbookModels.AbookMetaRaw>(sampleJson)

        assertEquals("Sherlock Holmes", parsed.title)
        assertEquals("Arthur Conan Doyle", parsed.author)
        assertEquals("Klassische Detektivgeschichte", parsed.description)
        assertEquals(2, parsed.chapters.size)
        assertEquals("01_intro.mp3", parsed.chapters[0].file)
        assertEquals("Kapitel 1: Einleitung", parsed.chapters[0].title)
    }

    @Test
    fun `parse metadata json with missing optional fields uses defaults`() {
        val sampleJson = """
            {
                "title": "Minimales Hörbuch"
            }
        """.trimIndent()

        val parsed = json.decodeFromString<AbookModels.AbookMetaRaw>(sampleJson)

        assertEquals("Minimales Hörbuch", parsed.title)
        assertEquals(null, parsed.author)
        assertEquals(null, parsed.description)
        assertNotNull(parsed.chapters)
        assertEquals(0, parsed.chapters.size)
    }
}
