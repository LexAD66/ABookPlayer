package de.f_soft_studio.abookplayer.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.f_soft_studio.abookplayer.data.local.db.AbookDatabase
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.usecase.GetListeningStatisticsUseCase
import de.f_soft_studio.abookplayer.domain.usecase.RecordListeningTimeUseCase
import de.f_soft_studio.abookplayer.domain.usecase.SaveProgressUseCase
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Unit-Test zur Verifikation des Hörstatistik-Features (DAO, Repository & UseCases).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ListeningStatisticsTest {

    private lateinit var database: AbookDatabase
    private lateinit var repository: AudiobookRepository
    private lateinit var recordListeningTimeUseCase: RecordListeningTimeUseCase
    private lateinit var saveProgressUseCase: SaveProgressUseCase
    private lateinit var getListeningStatisticsUseCase: GetListeningStatisticsUseCase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AbookDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = AudiobookRepository(
            audiobookDao = database.audiobookDao(),
            chapterDao = database.chapterDao(),
            bookmarkDao = database.bookmarkDao(),
            listeningSessionDao = database.listeningSessionDao()
        )

        recordListeningTimeUseCase = RecordListeningTimeUseCase(repository)
        saveProgressUseCase = SaveProgressUseCase(repository, recordListeningTimeUseCase)
        getListeningStatisticsUseCase = GetListeningStatisticsUseCase(repository)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testRecordListeningSessionAndGetTotal() = runBlocking {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        repository.recordListeningSession(audiobookId = 1L, durationSeconds = 300L, date = todayStr)
        repository.recordListeningSession(audiobookId = 1L, durationSeconds = 600L, date = todayStr)

        val totalSec = repository.getTotalListenTimeSeconds().first()
        val todaySec = repository.getListenTimeForDate(todayStr).first()

        assertEquals(900L, totalSec)
        assertEquals(900L, todaySec)
    }

    @Test
    fun testSaveProgressAutoRecordsListeningTime() = runBlocking {
        val book = Audiobook(
            title = "Statistik Test Buch",
            author = "Autor",
            filePath = "/path/test.zip"
        )
        val id = repository.saveAudiobook(book)

        // Initial Position 0 ms
        saveProgressUseCase(id, 0L)

        // Advance position by 10 seconds (10,000 ms)
        saveProgressUseCase(id, 10_000L)

        // Advance position by another 15 seconds (25,000 ms)
        saveProgressUseCase(id, 25_000L)

        val totalSec = repository.getTotalListenTimeSeconds().first()
        assertEquals(25L, totalSec)
    }

    @Test
    fun testGetListeningStatisticsUseCase() = runBlocking {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        repository.recordListeningSession(audiobookId = 10L, durationSeconds = 1200L, date = todayStr)

        val stats = getListeningStatisticsUseCase().first()

        assertNotNull(stats)
        assertEquals(1200L, stats.todaySeconds)
        assertEquals(1200L, stats.totalSeconds)
        assertEquals(1200L, stats.thisWeekSeconds)
        assertEquals(1, stats.currentStreakDays)
        assertEquals(7, stats.dailyActivityLast7Days.size)
    }
}
