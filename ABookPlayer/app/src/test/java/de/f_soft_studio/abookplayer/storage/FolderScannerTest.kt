package de.f_soft_studio.abookplayer.storage

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FolderScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = mockk(relaxed = true)
    private val folderScanner = FolderScanner(context)

    @Test
    fun `scanBookFolder merges CD1 and CD2 subfolders into single audiobook`() {
        val bookDir = tempFolder.newFolder("Der Herr der Ringe")

        val cd1Dir = File(bookDir, "CD1").apply { mkdirs() }
        val cd2Dir = File(bookDir, "CD2").apply { mkdirs() }

        File(cd1Dir, "01_Prolog.mp3").writeText("dummy audio content 1")
        File(cd1Dir, "02_Schatten_der_Vergangenheit.mp3").writeText("dummy audio content 2")

        File(cd2Dir, "01_Drei_ist_einer_zuviel.mp3").writeText("dummy audio content 3")
        File(cd2Dir, "02_Gesellschaft_im_Mondkalb.mp3").writeText("dummy audio content 4")

        val result = folderScanner.scanBookFolder(bookDir)

        assertNotNull(result)
        val audiobook = result!!.audiobook
        val chapters = result.chapters

        assertEquals("Der Herr der Ringe", audiobook.title)
        assertEquals(4, chapters.size)

        assertEquals("CD1 - 01_Prolog", chapters[0].title)
        assertEquals("CD1 - 02_Schatten_der_Vergangenheit", chapters[1].title)
        assertEquals("CD2 - 01_Drei_ist_einer_zuviel", chapters[2].title)
        assertEquals("CD2 - 02_Gesellschaft_im_Mondkalb", chapters[3].title)
    }

    @Test
    fun `scanBookFolder ignores non-audio files and detects cover image`() {
        val bookDir = tempFolder.newFolder("Harry Potter")

        File(bookDir, "cover.jpg").writeText("fake image data")
        File(bookDir, "notes.txt").writeText("some notes")
        File(bookDir, "Kapitel_01.mp3").writeText("audio data 1")
        File(bookDir, "Kapitel_02.flac").writeText("audio data 2")

        val result = folderScanner.scanBookFolder(bookDir)

        assertNotNull(result)
        assertEquals("Harry Potter", result!!.audiobook.title)
        assertNotNull(result.audiobook.coverUri)
        assertTrue(result.audiobook.coverUri!!.endsWith("cover.jpg"))
        assertEquals(2, result.chapters.size)
        assertEquals("Kapitel_01", result.chapters[0].title)
        assertEquals("Kapitel_02", result.chapters[1].title)
    }
}
