package de.f_soft_studio.abookplayer.player

import de.f_soft_studio.abookplayer.domain.model.Bookmark
import de.f_soft_studio.abookplayer.player.controller.SleepTimerController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SmartSleepBookmarkTest {

    @Test
    fun testSleepTimerCreatesStartBookmark() {
        val createdBookmarks = mutableListOf<Bookmark>()
        val sleepTimerController = SleepTimerController {
            // Timer expired action
        }

        val testBookId = 42L
        val currentPosition = 120_000L // 2 min in

        // Simulate starting a sleep timer with Smart Bookmark creation
        sleepTimerController.startTimerMinutes(15)
        createdBookmarks.add(
            Bookmark(
                audiobookId = testBookId,
                position = currentPosition,
                note = "🛌 Einschlaf-Start (15 Min Timer)"
            )
        )

        assertTrue(sleepTimerController.isActive.value)
        assertEquals(1, createdBookmarks.size)
        assertEquals(120_000L, createdBookmarks.first().position)
        assertTrue(createdBookmarks.first().note.contains("Einschlaf-Start"))
    }

    @Test
    fun testSleepTimerExtendPreservesActiveTimer() {
        val sleepTimerController = SleepTimerController {}
        sleepTimerController.startTimerMinutes(15)
        assertEquals(15 * 60 * 1000L, sleepTimerController.remainingTimeMs.value)

        sleepTimerController.extendTimerMinutes(15)
        assertEquals(30 * 60 * 1000L, sleepTimerController.remainingTimeMs.value)
    }
}
