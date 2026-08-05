package de.f_soft_studio.abookplayer.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Chapter
import de.f_soft_studio.abookplayer.domain.model.ExportState
import de.f_soft_studio.abookplayer.util.AbookModels
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AbookStorageExportTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var repository: AudiobookRepository
    private lateinit var abookStorage: AbookStorage

    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = mockk(relaxed = true)
        abookStorage = AbookStorage(context, repository)
    }

    @Test
    fun exportToAbook_createsValidCanonicalZipArchive() = runBlocking {
        // 1. Arrange dummy audio & cover files
        val dummyAudioFile = tempFolder.newFile("chapter1.mp3").apply {
            writeBytes("DUMMY AUDIO CONTENT".toByteArray())
        }
        val dummyCoverFile = tempFolder.newFile("cover.jpg").apply {
            writeBytes("DUMMY COVER IMAGE CONTENT".toByteArray())
        }

        val audiobook = Audiobook(
            id = 42L,
            title = "Test Hörbuch",
            author = "Test Autor",
            narrator = "Test Sprecher",
            filePath = dummyAudioFile.absolutePath,
            coverUri = dummyCoverFile.absolutePath,
            description = "Eine wunderbare Geschichte",
            duration = 120_000L
        )

        val chapters = listOf(
            Chapter(id = 1L, audiobookId = 42L, title = "Kapitel 1", startTime = 0L, audioPath = dummyAudioFile.absolutePath)
        )

        val outputFile = tempFolder.newFile("test_export.abook")

        // 2. Act
        val states = FileOutputStream(outputFile).use { outputStream ->
            abookStorage.exportToAbook(audiobook, chapters, outputStream).toList()
        }

        // 3. Assert export states
        assertTrue(states.any { it is ExportState.Exporting })
        val finalState = states.last()
        assertTrue("Final state should be Success but was $finalState", finalState is ExportState.Success)

        // 4. Verify ZIP content integrity
        ZipFile(outputFile).use { zip ->
            val manifestEntry = zip.getEntry("manifest.json")
            assertNotNull("manifest.json must exist in ZIP", manifestEntry)

            val manifestText = zip.getInputStream(manifestEntry).bufferedReader().readText()
            val manifest = json.decodeFromString<AbookModels.AbookManifestRaw>(manifestText)

            assertEquals(1, manifest.version)
            assertEquals("Test Hörbuch", manifest.title)
            assertEquals("Test Autor", manifest.author)
            assertEquals("Test Sprecher", manifest.narrator)
            assertEquals("Eine wunderbare Geschichte", manifest.description)
            assertEquals("cover.jpg", manifest.cover)
            assertEquals(1, manifest.tracks.size)
            assertEquals("Kapitel 1", manifest.tracks[0].title)

            val coverEntry = zip.getEntry("cover.jpg")
            assertNotNull("cover.jpg must exist in ZIP", coverEntry)

            val audioEntry = zip.getEntry(manifest.tracks[0].file)
            assertNotNull("Audio track entry must exist in ZIP", audioEntry)
        }
    }
}
