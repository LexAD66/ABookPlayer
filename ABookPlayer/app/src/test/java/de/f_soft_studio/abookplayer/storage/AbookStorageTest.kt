package de.f_soft_studio.abookplayer.storage

import android.content.Context
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit-Test zur Überprüfung von AbookStorage (Zip-Slip Schutz & Dateipfad-Validierung).
 */
class AbookStorageTest {

    private lateinit var context: Context
    private lateinit var repository: AudiobookRepository
    private lateinit var storage: AbookStorage
    private lateinit var tempBaseDir: File

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        storage = AbookStorage(context, repository)

        tempBaseDir = File(System.getProperty("java.io.tmpdir"), "abook_storage_test_${System.currentTimeMillis()}").apply {
            mkdirs()
        }
    }

    @Test
    fun `safeTargetFile returns file when path is within base directory`() {
        val validFileName = "chapter1.mp3"
        val result = storage.safeTargetFile(tempBaseDir, validFileName)

        assertNotNull(result)
        assertEquals(File(tempBaseDir.canonicalFile, validFileName).canonicalPath, result?.canonicalPath)
    }

    @Test
    fun `safeTargetFile handles flac files correctly`() {
        val validFlacName = "music_track.flac"
        val result = storage.safeTargetFile(tempBaseDir, validFlacName)

        assertNotNull(result)
        assertEquals(File(tempBaseDir.canonicalFile, validFlacName).canonicalPath, result?.canonicalPath)
    }

    @Test
    fun `safeTargetFile returns null when path traversal Zip-Slip is detected`() {
        val maliciousPath = "../../../outside_system_file.txt"
        val result = storage.safeTargetFile(tempBaseDir, maliciousPath)

        assertNull("Erwartet null bei Versuch der Pfad-Traversierung (Zip Slip)", result)
    }

    @Test
    fun `constants for zip bomb protection are correctly configured`() {
        assertEquals(4L * 1024 * 1024 * 1024, AbookStorage.MAX_SINGLE_FILE_SIZE_BYTES)
        assertEquals(20L * 1024 * 1024 * 1024, AbookStorage.MAX_TOTAL_UNCOMPRESSED_SIZE_BYTES)
        assertEquals(100L, AbookStorage.MAX_COMPRESSION_RATIO)
    }
}
