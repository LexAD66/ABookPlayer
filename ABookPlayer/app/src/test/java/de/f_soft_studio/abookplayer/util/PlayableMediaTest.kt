package de.f_soft_studio.abookplayer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PlayableMediaTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun isPlayableFile_echteDatei_true() {
        val f = tmp.newFile("kapitel1.mp3")
        assertTrue(PlayableMedia.isPlayableFile(f.absolutePath))
    }

    @Test
    fun isPlayableFile_verzeichnis_false() {
        val dir = tmp.newFolder("imported_1988008138")
        assertFalse(
            "Ein Verzeichnis darf nie als abspielbar gelten (sonst EISDIR in ExoPlayer)",
            PlayableMedia.isPlayableFile(dir.absolutePath)
        )
    }

    @Test
    fun isPlayableFile_fehlenderPfad_false() {
        assertFalse(PlayableMedia.isPlayableFile(tmp.root.absolutePath + "/gibtsnicht.mp3"))
    }

    @Test
    fun isPlayableFile_nullOderLeer_false() {
        assertFalse(PlayableMedia.isPlayableFile(null))
        assertFalse(PlayableMedia.isPlayableFile(""))
        assertFalse(PlayableMedia.isPlayableFile("   "))
    }

    @Test
    fun playableFiles_behaeltNurDateienUndReihenfolge() {
        val a = tmp.newFile("a.mp3")
        tmp.newFolder("ordner")
        val c = tmp.newFile("c.mp3")
        val result = PlayableMedia.playableFiles(
            listOf(
                a.absolutePath,
                tmp.root.absolutePath + "/ordner",
                null,
                "   ",
                tmp.root.absolutePath + "/fehlt.mp3",
                c.absolutePath
            )
        )
        assertEquals(listOf(a, c), result)
    }
}
