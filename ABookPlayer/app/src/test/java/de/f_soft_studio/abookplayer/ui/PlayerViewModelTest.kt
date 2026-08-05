package de.f_soft_studio.abookplayer.ui

import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.player.controller.PlaybackController
import de.f_soft_studio.abookplayer.ui.player.PlayerViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit-Test für PlayerViewModel (Play/Pause, Seeking, Speed).
 */
class PlayerViewModelTest {

    private lateinit var playbackController: PlaybackController
    private lateinit var viewModel: PlayerViewModel

    private val currentAudiobookState = MutableStateFlow<Audiobook?>(null)
    private val isPlayingState = MutableStateFlow(false)
    private val currentPositionState = MutableStateFlow(0L)
    private val durationState = MutableStateFlow(120000L)
    private val speedState = MutableStateFlow(1.0f)

    @Before
    fun setup() {
        playbackController = mockk(relaxed = true)
        every { playbackController.currentAudiobook } returns currentAudiobookState
        every { playbackController.isPlaying } returns isPlayingState
        every { playbackController.currentPosition } returns currentPositionState
        every { playbackController.duration } returns durationState
        every { playbackController.playbackSpeed } returns speedState

        viewModel = PlayerViewModel(playbackController)
    }

    @Test
    fun testTogglePlayPauseCallsController() {
        viewModel.togglePlayPause()
        verify(exactly = 1) { playbackController.togglePlayPause() }
    }

    @Test
    fun testSkip10sForwardCallsController() {
        viewModel.skip10sForward()
        verify(exactly = 1) { playbackController.skip10SecondsForward() }
    }

    @Test
    fun testSkip10sBackwardCallsController() {
        viewModel.skip10sBackward()
        verify(exactly = 1) { playbackController.skip10SecondsBackward() }
    }

    @Test
    fun testSeekToCallsController() {
        viewModel.seekTo(45000L)
        verify(exactly = 1) { playbackController.seekTo(45000L) }
    }

    @Test
    fun testSetSpeedCallsController() {
        viewModel.setSpeed(1.5f)
        verify(exactly = 1) { playbackController.setPlaybackSpeed(1.5f) }
    }

    @Test
    fun testSkipChapterCallsController() {
        viewModel.skipNextChapter()
        verify(exactly = 1) { playbackController.skipToNextChapter() }

        viewModel.skipPreviousChapter()
        verify(exactly = 1) { playbackController.skipToPreviousChapter() }
    }
}
