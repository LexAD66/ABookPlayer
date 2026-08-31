package de.f_soft_studio.abookplayer.player

import de.f_soft_studio.abookplayer.player.controller.AudioPreset
import de.f_soft_studio.abookplayer.player.controller.LoudnessController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoudnessControllerTest {

    private lateinit var loudnessController: LoudnessController

    @Before
    fun setUp() {
        loudnessController = LoudnessController()
    }

    @Test
    fun testDefaultPresetIsVoiceBoost() {
        assertEquals(AudioPreset.VOICE_BOOST, loudnessController.currentPreset.value)
    }

    @Test
    fun testSetPresetChangesPreset() {
        loudnessController.setPreset(AudioPreset.BASS_REDUCTION)
        assertEquals(AudioPreset.BASS_REDUCTION, loudnessController.currentPreset.value)

        loudnessController.setPreset(AudioPreset.NIGHT_MODE)
        assertEquals(AudioPreset.NIGHT_MODE, loudnessController.currentPreset.value)
    }

    @Test
    fun testSetLoudnessEnabled() {
        assertFalse(loudnessController.isLoudnessEnabled.value)
        loudnessController.setLoudnessEnabled(true)
        assertTrue(loudnessController.isLoudnessEnabled.value)
    }

    @Test
    fun testSetLoudnessGainDbClampsValues() {
        loudnessController.setLoudnessGainDb(6)
        assertEquals(600, loudnessController.loudnessGainMb.value)

        loudnessController.setLoudnessGainDb(20) // should clamp to 1500 mB (15 dB)
        assertEquals(1500, loudnessController.loudnessGainMb.value)

        loudnessController.setLoudnessGainDb(-5) // should clamp to 0 mB (0 dB)
        assertEquals(0, loudnessController.loudnessGainMb.value)
    }

    @Test
    fun testSetCustomBandGain() {
        loudnessController.setCustomBandGain(0, 300)
        assertEquals(300.toShort(), loudnessController.customBandGains.value[0])
    }

    @Test
    fun testReleaseCleansUpWithoutExceptions() {
        loudnessController.release()
        assertFalse(loudnessController.isLoudnessEnabled.value)
    }
}
