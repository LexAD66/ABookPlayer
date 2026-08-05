package de.f_soft_studio.abookplayer.storage

import de.f_soft_studio.abookplayer.util.AbookModels
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit-Test zur Validierung des kanonischen manifest.json (Format v1) sowie des Legacy-Formats metadata.json.
 */
class AbookManifestParserTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse canonical manifest json v1 returns full AbookManifestRaw object`() {
        val sampleJson = """
            {
                "version": 1,
                "id": "book_sherlock_001",
                "title": "Der Hund von Baskerville",
                "author": "Arthur Conan Doyle",
                "narrator": "Erika Muster",
                "description": "Klassischer Kriminalroman",
                "cover": "cover.jpg",
                "totalDuration": 4500000,
                "tracks": [
                    { "file": "audio/01_intro.mp3", "title": "Kapitel 1: Der Einstieg", "duration": 1500000 },
                    { "file": "audio/02_moor.mp3", "title": "Kapitel 2: Im Moor", "duration": 3000000 }
                ]
            }
        """.trimIndent()

        val manifest = json.decodeFromString<AbookModels.AbookManifestRaw>(sampleJson)

        assertEquals(1, manifest.version)
        assertEquals("book_sherlock_001", manifest.id)
        assertEquals("Der Hund von Baskerville", manifest.title)
        assertEquals("Arthur Conan Doyle", manifest.author)
        assertEquals("Erika Muster", manifest.narrator)
        assertEquals("Klassischer Kriminalroman", manifest.description)
        assertEquals("cover.jpg", manifest.cover)
        assertEquals(4500000L, manifest.totalDuration)
        assertEquals(2, manifest.tracks.size)
        assertEquals("audio/01_intro.mp3", manifest.tracks[0].file)
        assertEquals("Kapitel 1: Der Einstieg", manifest.tracks[0].title)
    }

    @Test
    fun `parse minimal manifest json uses default values`() {
        val sampleJson = """
            {
                "title": "Minimales Hörbuch"
            }
        """.trimIndent()

        val manifest = json.decodeFromString<AbookModels.AbookManifestRaw>(sampleJson)

        assertEquals(1, manifest.version)
        assertEquals("Minimales Hörbuch", manifest.title)
        assertNull(manifest.author)
        assertNull(manifest.narrator)
        assertNull(manifest.description)
        assertNotNull(manifest.tracks)
        assertEquals(0, manifest.tracks.size)
    }

    @Test
    fun `parse legacy metadata json returns legacy AbookMetaRaw object`() {
        val legacyJson = """
            {
                "title": "Alt-Format Hörbuch",
                "author": "Legacy Autor",
                "description": "Veraltetes Format",
                "chapters": [
                    { "file": "01.mp3", "title": "Kapitel A" }
                ]
            }
        """.trimIndent()

        val legacyMeta = json.decodeFromString<AbookModels.AbookMetaRaw>(legacyJson)

        assertEquals("Alt-Format Hörbuch", legacyMeta.title)
        assertEquals("Legacy Autor", legacyMeta.author)
        assertEquals(1, legacyMeta.chapters.size)
        assertEquals("01.mp3", legacyMeta.chapters[0].file)
    }
}
