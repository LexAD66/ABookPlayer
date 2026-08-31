package de.f_soft_studio.abookplayer.player.controller

import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AudioPreset: Verfügbare Klangprofile für die Hörbuchwiedergabe.
 */
enum class AudioPreset(val labelDe: String, val labelEn: String, val description: String) {
    STANDARD("Standard (Flat)", "Standard (Flat)", "Ausgewogener Klang ohne zusätzliche Filterung."),
    VOICE_BOOST("Sprachklarheit", "Voice Boost", "Mittenanhebung für optimale Verständlichkeit von Sprechern."),
    BASS_REDUCTION("Bass-Reduzierung", "Bass Reduction", "Dämpft störenden Tieftonbereich z.B. im Auto oder Kopfhörer."),
    NIGHT_MODE("Nachtmodus", "Night Mode", "Gedämpfte Höhen und weicher Klang für entspanntes Hören im Bett."),
    CUSTOM("Benutzerdefiniert", "Custom", "Manuelle Anpassung des 5-Band Equalizers.")
}

/**
 * LoudnessController: Steuert den Android LoudnessEnhancer (+0 bis +15 dB Gain)
 * sowie den 5-Band Equalizer für Hörbücher.
 */
class LoudnessController {
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var equalizer: Equalizer? = null
    private var currentAudioSessionId: Int = -1

    private val _currentPreset = MutableStateFlow(AudioPreset.VOICE_BOOST)
    val currentPreset: StateFlow<AudioPreset> = _currentPreset.asStateFlow()

    private val _loudnessGainMb = MutableStateFlow(600) // Default +6 dB Boost
    val loudnessGainMb: StateFlow<Int> = _loudnessGainMb.asStateFlow()

    private val _isLoudnessEnabled = MutableStateFlow(false)
    val isLoudnessEnabled: StateFlow<Boolean> = _isLoudnessEnabled.asStateFlow()

    private val _customBandGains = MutableStateFlow(shortArrayOf(0, 0, 0, 0, 0))
    val customBandGains: StateFlow<ShortArray> = _customBandGains.asStateFlow()

    /**
     * Bindet den LoudnessEnhancer und Equalizer an die aktuelle ExoPlayer Audio-Session an.
     */
    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        if (audioSessionId == currentAudioSessionId && loudnessEnhancer != null) {
            applyLoudness()
            applyPreset(_currentPreset.value)
            return
        }

        release()
        currentAudioSessionId = audioSessionId

        try {
            loudnessEnhancer = LoudnessEnhancer(audioSessionId)
            applyLoudness()
        } catch (_: Exception) {}

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            applyPreset(_currentPreset.value)
        } catch (_: Exception) {}
    }

    /**
     * Schaltet die Lautstärke-Verstärkung (LoudnessEnhancer) ein oder aus.
     */
    fun setLoudnessEnabled(enabled: Boolean) {
        _isLoudnessEnabled.value = enabled
        applyLoudness()
    }

    /**
     * Setzt die Lautstärke-Verstärkung in dB (z.B. 0 bis 15 dB).
     */
    fun setLoudnessGainDb(gainDb: Int) {
        _loudnessGainMb.value = (gainDb * 100).coerceIn(0, 1500)
        applyLoudness()
    }

    /**
     * Wählt ein Equalizer-Preset aus.
     */
    fun setPreset(preset: AudioPreset) {
        _currentPreset.value = preset
        applyPreset(preset)
    }

    /**
     * Setzt die Verstärkung für ein einzelnes Band im Custom-Modus.
     */
    fun setCustomBandGain(bandIndex: Int, gainMb: Short) {
        val current = _customBandGains.value.copyOf()
        if (bandIndex in current.indices) {
            current[bandIndex] = gainMb
            _customBandGains.value = current
            if (_currentPreset.value == AudioPreset.CUSTOM) {
                applyCustomBands()
            }
        }
    }

    private fun applyLoudness() {
        try {
            loudnessEnhancer?.let { enhancer ->
                if (_isLoudnessEnabled.value && _loudnessGainMb.value > 0) {
                    enhancer.setTargetGain(_loudnessGainMb.value)
                    enhancer.enabled = true
                } else {
                    enhancer.enabled = false
                }
            }
        } catch (_: Exception) {}
    }

    private fun applyPreset(preset: AudioPreset) {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            if (numBands <= 0) return

            val range = eq.bandLevelRange
            val minLevel = range[0]
            val maxLevel = range[1]

            when (preset) {
                AudioPreset.STANDARD -> {
                    for (i in 0 until numBands) {
                        eq.setBandLevel(i.toShort(), 0.toShort())
                    }
                }
                AudioPreset.VOICE_BOOST -> {
                    for (i in 0 until numBands) {
                        val freq = eq.getCenterFreq(i.toShort()) / 1000 // in Hz
                        val targetGain = when {
                            freq < 200 -> -300 // -3 dB sub-bass
                            freq in 500..3500 -> 500 // +5 dB voice clarity
                            freq > 8000 -> -100 // slight treble roll-off
                            else -> 0
                        }.toShort().coerceIn(minLevel, maxLevel)
                        eq.setBandLevel(i.toShort(), targetGain)
                    }
                }
                AudioPreset.BASS_REDUCTION -> {
                    for (i in 0 until numBands) {
                        val freq = eq.getCenterFreq(i.toShort()) / 1000 // in Hz
                        val targetGain = when {
                            freq < 150 -> -600 // -6 dB
                            freq < 400 -> -300 // -3 dB
                            else -> 0
                        }.toShort().coerceIn(minLevel, maxLevel)
                        eq.setBandLevel(i.toShort(), targetGain)
                    }
                }
                AudioPreset.NIGHT_MODE -> {
                    for (i in 0 until numBands) {
                        val freq = eq.getCenterFreq(i.toShort()) / 1000
                        val targetGain = when {
                            freq < 200 -> -400
                            freq in 800..3000 -> 200
                            freq > 5000 -> -500
                            else -> 0
                        }.toShort().coerceIn(minLevel, maxLevel)
                        eq.setBandLevel(i.toShort(), targetGain)
                    }
                }
                AudioPreset.CUSTOM -> {
                    applyCustomBands()
                }
            }
        } catch (_: Exception) {}
    }

    private fun applyCustomBands() {
        val eq = equalizer ?: return
        try {
            val gains = _customBandGains.value
            val numBands = eq.numberOfBands.toInt()
            val range = eq.bandLevelRange
            val minLevel = range[0]
            val maxLevel = range[1]

            for (i in 0 until minOf(numBands, gains.size)) {
                val clamped = gains[i].coerceIn(minLevel, maxLevel)
                eq.setBandLevel(i.toShort(), clamped)
            }
        } catch (_: Exception) {}
    }

    /**
     * Gibt Ressourcen des LoudnessEnhancers und Equalizers frei.
     */
    fun release() {
        try {
            loudnessEnhancer?.release()
        } catch (_: Exception) {}
        loudnessEnhancer = null

        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null
        currentAudioSessionId = -1
    }
}
