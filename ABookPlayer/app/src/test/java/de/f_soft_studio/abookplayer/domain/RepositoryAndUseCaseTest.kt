package de.f_soft_studio.abookplayer.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.f_soft_studio.abookplayer.data.local.db.AbookDatabase
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.usecase.AddBookmarkUseCase
import de.f_soft_studio.abookplayer.domain.usecase.DeleteAudiobookUseCase
import de.f_soft_studio.abookplayer.domain.usecase.GetAudiobooksUseCase
import de.f_soft_studio.abookplayer.domain.usecase.SaveProgressUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit-Test zur Überprüfung von Repository und UseCases.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RepositoryAndUseCaseTest {

    private lateinit var database: AbookDatabase
    private lateinit var repository: AudiobookRepository
    private lateinit var getAudiobooksUseCase: GetAudiobooksUseCase
    private lateinit var saveProgressUseCase: SaveProgressUseCase
    private lateinit var addBookmarkUseCase: AddBookmarkUseCase
    private lateinit var deleteAudiobookUseCase: DeleteAudiobookUseCase

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

        getAudiobooksUseCase = GetAudiobooksUseCase(repository)
        saveProgressUseCase = SaveProgressUseCase(repository)
        addBookmarkUseCase = AddBookmarkUseCase(repository)
        deleteAudiobookUseCase = DeleteAudiobookUseCase(repository)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testSaveAudiobookAndGetAudiobooksUseCase() = runBlocking {
        val audiobook = Audiobook(
            title = "UseCase Test Buch",
            author = "Test Autor",
            filePath = "/path/test.zip",
            coverUri = null,
            duration = 60000L,
            currentPosition = 0L,
            lastPlayed = System.currentTimeMillis()
        )

        val id = repository.saveAudiobook(audiobook)
        val books = getAudiobooksUseCase().first()

        assertEquals(1, books.size)
        assertEquals("UseCase Test Buch", books[0].title)
        assertEquals(id, books[0].id)
    }

    @Test
    fun testSaveProgressAndUpdatePosition() = runBlocking {
        val audiobook = Audiobook(
            title = "Fortschritts Test",
            author = "Autor",
            filePath = "/path/progress.zip",
            coverUri = null,
            duration = 120000L,
            currentPosition = 0L,
            lastPlayed = System.currentTimeMillis()
        )

        val id = repository.saveAudiobook(audiobook)
        saveProgressUseCase(id, 45000L)

        val updated = repository.getAudiobookById(id)
        assertEquals(45000L, updated?.currentPosition)
    }

    @Test
    fun testAddBookmarkAndCascadeDelete() = runBlocking {
        val audiobook = Audiobook(
            title = "Bookmark & Delete Test",
            author = "Autor",
            filePath = "/path/delete.zip",
            coverUri = null,
            duration = 180000L,
            currentPosition = 10000L,
            lastPlayed = System.currentTimeMillis()
        )

        val bookId = repository.saveAudiobook(audiobook)
        val bookmarkId = addBookmarkUseCase(bookId, 15000L, "Spannende Stelle")

        val bookmarksBefore = repository.getBookmarksForAudiobook(bookId).first()
        assertEquals(1, bookmarksBefore.size)
        assertEquals("Spannende Stelle", bookmarksBefore[0].note)

        deleteAudiobookUseCase(bookId)

        val deletedBook = repository.getAudiobookById(bookId)
        assertNull(deletedBook)

        val bookmarksAfter = repository.getBookmarksForAudiobook(bookId).first()
        assertEquals(0, bookmarksAfter.size)
    }
}
