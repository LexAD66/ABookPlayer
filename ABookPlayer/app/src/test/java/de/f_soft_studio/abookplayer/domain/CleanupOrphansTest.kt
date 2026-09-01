package de.f_soft_studio.abookplayer.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.f_soft_studio.abookplayer.data.local.db.AbookDatabase
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Prüft, dass [AudiobookRepository.cleanupDuplicatesAndOrphans] Hörbücher entfernt,
 * deren Ordner leer ist (Dateien verschoben/gelöscht) – nicht nur solche mit
 * komplett fehlendem Pfad.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CleanupOrphansTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var database: AbookDatabase
    private lateinit var repository: AudiobookRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AbookDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = AudiobookRepository(
            audiobookDao = database.audiobookDao(),
            chapterDao = database.chapterDao(),
            bookmarkDao = database.bookmarkDao()
        )
    }

    @After
    fun teardown() = database.close()

    @Test
    fun leererOrdnerWirdAlsVerwaistEntfernt_ordnerMitAudioBleibt() = runBlocking {
        val emptyDir = tmp.newFolder("imported_empty")
        val goodDir = tmp.newFolder("imported_good")
        java.io.File(goodDir, "001 - Kapitel.mp3").createNewFile()

        repository.saveAudiobook(
            Audiobook(title = "Kaputt (Edition)", author = "A", filePath = emptyDir.absolutePath, duration = 0L)
        )
        repository.saveAudiobook(
            Audiobook(title = "Heil", author = "A", filePath = goodDir.absolutePath, duration = 1_000L)
        )

        val result = repository.cleanupDuplicatesAndOrphans(ApplicationProvider.getApplicationContext())

        assertEquals("leerer Ordner = 1 verwaistes Buch", 1, result.orphansRemoved)
        val remaining = repository.getAllAudiobooks().first()
        assertEquals(1, remaining.size)
        assertEquals("Heil", remaining.first().title)
    }

    @Test
    fun fehlenderPfadWirdWeiterhinEntfernt() = runBlocking {
        repository.saveAudiobook(
            Audiobook(
                title = "Weg", author = "A",
                filePath = tmp.root.absolutePath + "/gibtsnicht", duration = 0L
            )
        )
        val result = repository.cleanupDuplicatesAndOrphans(ApplicationProvider.getApplicationContext())
        assertEquals(1, result.orphansRemoved)
        assertTrue(repository.getAllAudiobooks().first().isEmpty())
    }
}
