package de.f_soft_studio.abookplayer.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.f_soft_studio.abookplayer.data.local.dao.AudiobookDao
import de.f_soft_studio.abookplayer.data.local.dao.BookmarkDao
import de.f_soft_studio.abookplayer.data.local.db.AbookDatabase
import de.f_soft_studio.abookplayer.data.local.entity.AudiobookEntity
import de.f_soft_studio.abookplayer.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit-Test für Room DAOs (AudiobookDao, BookmarkDao) mit einer In-Memory SQLite-Datenbank.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RoomDaoTest {

    private lateinit var database: AbookDatabase
    private lateinit var audiobookDao: AudiobookDao
    private lateinit var bookmarkDao: BookmarkDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AbookDatabase::class.java
        ).allowMainThreadQueries().build()

        audiobookDao = database.audiobookDao()
        bookmarkDao = database.bookmarkDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAudiobook() = runBlocking {
        val audiobook = AudiobookEntity(
            title = "Test Hörbuch",
            author = "Test Autor",
            filePath = "/path/test.abook",
            coverUri = null,
            duration = 120000L,
            currentPosition = 0L,
            lastPlayed = System.currentTimeMillis()
        )

        val id = audiobookDao.insertAudiobook(audiobook)
        val loaded = audiobookDao.getAudiobookById(id)

        assertNotNull(loaded)
        assertEquals("Test Hörbuch", loaded?.title)
        assertEquals("Test Autor", loaded?.author)
    }

    @Test
    fun insertBookmarkAndFetchFlow() = runBlocking {
        val audiobook = AudiobookEntity(
            title = "Hörbuch mit Bookmark",
            author = "Autor",
            filePath = "/path/test2.abook",
            coverUri = null,
            duration = 300000L,
            currentPosition = 5000L,
            lastPlayed = System.currentTimeMillis()
        )

        val bookId = audiobookDao.insertAudiobook(audiobook)

        val bookmark = BookmarkEntity(
            audiobookId = bookId,
            position = 45000L,
            note = "Wichtige Stelle",
            createdAt = System.currentTimeMillis()
        )

        bookmarkDao.insertBookmark(bookmark)

        val bookmarks = bookmarkDao.getBookmarksForAudiobookFlow(bookId).first()

        assertEquals(1, bookmarks.size)
        assertEquals("Wichtige Stelle", bookmarks[0].note)
        assertEquals(45000L, bookmarks[0].position)
    }
}
