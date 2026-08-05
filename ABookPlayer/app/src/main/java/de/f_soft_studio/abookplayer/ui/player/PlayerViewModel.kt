package de.f_soft_studio.abookplayer.ui.player

import androidx.lifecycle.ViewModel
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Chapter
import de.f_soft_studio.abookplayer.player.controller.PlaybackController
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel zur Steuerung und Zustandsbereistellung des PlayerScreens.
 */
class PlayerViewModel(
    val playbackController: PlaybackController
) : ViewModel() {

    val currentAudiobook: StateFlow<Audiobook?> = playbackController.currentAudiobook
    val isPlaying: StateFlow<Boolean> = playbackController.isPlaying
    val currentPosition: StateFlow<Long> = playbackController.currentPosition
    val duration: StateFlow<Long> = playbackController.duration
    val playbackSpeed: StateFlow<Float> = playbackController.playbackSpeed
    val chapters: StateFlow<List<Chapter>> = playbackController.chapters
    val currentChapter: StateFlow<Chapter?> = playbackController.currentChapter
    val isVolumeBoostEnabled: StateFlow<Boolean> = playbackController.isVolumeBoostEnabled
    val isSkipSilenceEnabled: StateFlow<Boolean> = playbackController.isSkipSilenceEnabled

    private var lastPauseTimeMs: Long = 0

    fun loadAudiobook(audiobook: Audiobook, chapters: List<Chapter> = emptyList()) {
        playbackController.loadAudiobook(audiobook, chapters, autoPlay = true)
    }

    fun togglePlayPause(autoRewindSec: Int = 10) {
        if (!isPlaying.value) {
            if (lastPauseTimeMs > 0 && (System.currentTimeMillis() - lastPauseTimeMs) > 30_000L && autoRewindSec > 0) {
                val rewindPos = (currentPosition.value - (autoRewindSec * 1000L)).coerceAtLeast(0L)
                seekTo(rewindPos)
            }
        } else {
            lastPauseTimeMs = System.currentTimeMillis()
        }
        playbackController.togglePlayPause()
    }

    fun skip10sForward() {
        playbackController.skip10SecondsForward()
    }

    fun skip10sBackward() {
        playbackController.skip10SecondsBackward()
    }

    fun skipNextChapter() {
        playbackController.skipToNextChapter()
    }

    fun skipPreviousChapter() {
        playbackController.skipToPreviousChapter()
    }

    fun seekTo(positionMs: Long) {
        playbackController.seekTo(positionMs)
    }

    fun setSpeed(speed: Float) {
        playbackController.setPlaybackSpeed(speed)
    }

    fun toggleVolumeBoost() {
        playbackController.toggleVolumeBoost()
    }

    fun toggleSkipSilence() {
        playbackController.toggleSkipSilence()
    }
}
