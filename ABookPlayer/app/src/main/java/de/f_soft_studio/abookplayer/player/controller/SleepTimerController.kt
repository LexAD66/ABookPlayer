package de.f_soft_studio.abookplayer.player.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Controller zur Verwaltung des Sleep-Timers (Sleeptimer / Einschlafautomatik).
 */
class SleepTimerController(
    private val onTimerExpired: () -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private val _remainingTimeMs = MutableStateFlow<Long>(0L)
    val remainingTimeMs: StateFlow<Long> = _remainingTimeMs.asStateFlow()

    private val _stopAtEndOfChapter = MutableStateFlow(false)
    val stopAtEndOfChapter: StateFlow<Boolean> = _stopAtEndOfChapter.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    /**
     * Startet den Sleep-Timer für die angegebene Anzahl an Minuten.
     */
    fun startTimerMinutes(minutes: Int) {
        startTimerMs(minutes * 60 * 1000L)
    }

    /**
     * Startet den Sleep-Timer für das Ende des aktuellen Kapitels.
     */
    fun startTimerEndOfChapter() {
        cancelTimer()
        _stopAtEndOfChapter.value = true
        _isActive.value = true
    }

    /**
     * Startet den Sleep-Timer mit einer genauen Dauer in Millisekunden.
     */
    fun startTimerMs(durationMs: Long) {
        cancelTimer()
        _remainingTimeMs.value = durationMs
        _isActive.value = true

        timerJob = scope.launch {
            while (_remainingTimeMs.value > 0) {
                delay(1000L)
                _remainingTimeMs.value = (_remainingTimeMs.value - 1000L).coerceAtLeast(0L)
            }
            _isActive.value = false
            onTimerExpired()
        }
    }

    /**
     * Verlängert den laufenden Timer um die angegebene Anzahl an Minuten (z. B. +15 Min bei Schütteln).
     */
    fun extendTimerMinutes(minutes: Int = 15) {
        if (_isActive.value) {
            _remainingTimeMs.value += minutes * 60 * 1000L
        } else {
            startTimerMinutes(minutes)
        }
    }

    /**
     * Bricht den laufenden Sleep-Timer ab.
     */
    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _remainingTimeMs.value = 0L
        _stopAtEndOfChapter.value = false
        _isActive.value = false
    }

    /**
     * Wird aufgerufen, wenn ein Kapitelende erreicht wird und stopAtEndOfChapter aktiv ist.
     */
    fun triggerChapterEndExpired() {
        if (_stopAtEndOfChapter.value) {
            cancelTimer()
            onTimerExpired()
        }
    }
}
