package de.f_soft_studio.abookplayer.player

import de.f_soft_studio.abookplayer.player.controller.SleepTimerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit-Test für SleepTimerController (Start, Abbruch, Restzeit).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SleepTimerControllerTest {

    private var expiredCount = 0
    private lateinit var controller: SleepTimerController

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        expiredCount = 0
        controller = SleepTimerController {
            expiredCount++
        }
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStartTimerMinutesSetsState() {
        assertFalse(controller.isActive.value)
        assertEquals(0L, controller.remainingTimeMs.value)

        controller.startTimerMinutes(15)

        assertTrue(controller.isActive.value)
        assertEquals(15 * 60 * 1000L, controller.remainingTimeMs.value)
    }

    @Test
    fun testCancelTimerResetsState() {
        controller.startTimerMinutes(30)
        assertTrue(controller.isActive.value)

        controller.cancelTimer()

        assertFalse(controller.isActive.value)
        assertEquals(0L, controller.remainingTimeMs.value)
    }

    @Test
    fun testStartTimerEndOfChapterAndTrigger() {
        assertFalse(controller.stopAtEndOfChapter.value)
        controller.startTimerEndOfChapter()

        assertTrue(controller.isActive.value)
        assertTrue(controller.stopAtEndOfChapter.value)
        assertEquals(0, expiredCount)

        controller.triggerChapterEndExpired()

        assertFalse(controller.isActive.value)
        assertFalse(controller.stopAtEndOfChapter.value)
        assertEquals(1, expiredCount)
    }
}
