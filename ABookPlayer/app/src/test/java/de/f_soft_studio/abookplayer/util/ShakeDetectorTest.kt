package de.f_soft_studio.abookplayer.util

import org.junit.Assert.*
import org.junit.Test

class ShakeDetectorTest {

    @Test
    fun `shake detector initializes without crashing`() {
        var count = 0
        val detector = ShakeDetector { count++ }
        assertNotNull(detector)
        assertEquals(0, count)
    }
}
