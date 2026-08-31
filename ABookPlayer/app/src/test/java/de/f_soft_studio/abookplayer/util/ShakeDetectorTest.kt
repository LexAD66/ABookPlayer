package de.f_soft_studio.abookplayer.util

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ShakeDetectorTest {

    @Test
    fun `shake detector initializes without crashing`() {
        val mockContext = mockk<Context>(relaxed = true)
        var count = 0
        val detector = ShakeDetector(mockContext) { count++ }
        assertNotNull(detector)
        assertEquals(0, count)
    }
}


