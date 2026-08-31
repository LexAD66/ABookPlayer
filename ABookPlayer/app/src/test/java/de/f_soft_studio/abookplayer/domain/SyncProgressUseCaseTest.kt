package de.f_soft_studio.abookplayer.domain

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.usecase.SyncProgressUseCase
import de.f_soft_studio.abookplayer.storage.sync.SyncAudiobookProgressDto
import de.f_soft_studio.abookplayer.storage.sync.SyncStateDto
import de.f_soft_studio.abookplayer.storage.sync.WebDavSyncManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncProgressUseCaseTest {

    private val repository: AudiobookRepository = mockk(relaxed = true)
    private val webDavSyncManager: WebDavSyncManager = mockk(relaxed = true)
    private lateinit var syncProgressUseCase: SyncProgressUseCase

    @Before
    fun setUp() {
        syncProgressUseCase = SyncProgressUseCase(repository, webDavSyncManager)
    }

    @Test
    fun testInvokeReturnsErrorWhenNotConfigured() = runTest {
        every { webDavSyncManager.isConfigured } returns false

        val result = syncProgressUseCase()

        assertFalse(result.isSuccess)
        assertEquals("WebDAV-Zugangsdaten nicht konfiguriert.", result.message)
    }

    @Test
    fun testInvokeMergesRemoteProgressWhenRemoteIsNewer() = runTest {
        every { webDavSyncManager.isConfigured } returns true
        every { webDavSyncManager.deviceId } returns "test_device_1"

        val localBook = Audiobook(
            id = 101,
            title = "Der Herr der Ringe",
            author = "J.R.R. Tolkien",
            filePath = "/path/to/audiobook.abook",
            currentPosition = 5000L,
            lastPlayed = 1000L
        )

        every { repository.getAllAudiobooks() } returns flowOf(listOf(localBook))
        every { repository.getBookmarksForAudiobook(101) } returns flowOf(emptyList())

        val remoteBook = SyncAudiobookProgressDto(
            id = 1,
            title = "Der Herr der Ringe",
            author = "J.R.R. Tolkien",
            currentPosition = 25000L,
            lastPlayed = 2000L // Remote is newer
        )
        val remoteState = SyncStateDto(
            version = 1,
            deviceId = "remote_device",
            lastSyncedAt = 2000L,
            audiobooks = listOf(remoteBook)
        )

        coEvery { webDavSyncManager.downloadSyncState() } returns Result.success(remoteState)
        coEvery { webDavSyncManager.uploadSyncState(any()) } returns Result.success(true)

        val result = syncProgressUseCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.updatedAudiobooksCount)

        coVerify {
            repository.saveAudiobook(match {
                it.id == 101L && it.currentPosition == 25000L && it.lastPlayed == 2000L
            })
        }
    }
}
