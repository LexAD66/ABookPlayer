package de.f_soft_studio.abookplayer.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.f_soft_studio.abookplayer.player.controller.AudioPreset

/**
 * AudioProfile (Kompatibilitäts-Enum für abwärtskompatible Aufrufe)
 */
enum class AudioProfile(val label: String, val description: String) {
    STANDARD("Standard", "Ausgewogener Klang ohne zusätzliche Filterung."),
    SPRACHKLARHEIT("Sprachklarheit", "Mitten-Anhebung für besonders deutliche Stimmen."),
    BASS_BOOST("Tiefer Klang", "Warmes Klangbild mit verstärktem Tieftonbereich."),
    HOEHEN_BOOST("Prägnante Höhen", "Verstärkte Höhen für leise oder gedämpfte Sprecher.")
}

/**
 * Enhanced EqualizerDialog mit Auswahl von Equalizer-Presets, Lautstärke-Boost (+0 bis +15 dB)
 * und Stille überspringen (Skip Silence).
 */
@Composable
fun EqualizerDialog(
    currentPreset: AudioPreset = AudioPreset.VOICE_BOOST,
    loudnessGainDb: Int = 6,
    isVolumeBoostEnabled: Boolean = false,
    isSkipSilenceEnabled: Boolean = false,
    onPresetSelected: (AudioPreset) -> Unit = {},
    onLoudnessGainChanged: (Int) -> Unit = {},
    onVolumeBoostToggled: () -> Unit = {},
    onSkipSilenceToggled: () -> Unit = {},
    // Legacy support:
    currentProfile: AudioProfile = AudioProfile.SPRACHKLARHEIT,
    onProfileSelected: ((AudioProfile) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf(currentPreset) }
    var sliderGainDb by remember { mutableFloatStateOf(loudnessGainDb.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sound, Equalizer & Boost",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section: Loudness Gain (Voice Boost)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lautstärke-Verstärkung (Boost)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = isVolumeBoostEnabled,
                                onCheckedChange = { onVolumeBoostToggled() },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                        if (isVolumeBoostEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Zusätzliche Verstärkung: +${sliderGainDb.toInt()} dB",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Slider(
                                value = sliderGainDb,
                                onValueChange = {
                                    sliderGainDb = it
                                    onLoudnessGainChanged(it.toInt())
                                },
                                valueRange = 1f..15f,
                                steps = 13,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // Section: Skip Silence (Stille überspringen)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stille überspringen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Überspringt automatisch stumme Abschnitte.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isSkipSilenceEnabled,
                            onCheckedChange = { onSkipSilenceToggled() },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // Section: Equalizer Presets
                Text(
                    text = "Klangprofile / Equalizer Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                AudioPreset.values().forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPreset = preset
                                onPresetSelected(preset)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedPreset = preset
                                    onPresetSelected(preset)
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = preset.labelDe,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = preset.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPresetSelected(selectedPreset)
                    onProfileSelected?.invoke(
                        when (selectedPreset) {
                            AudioPreset.VOICE_BOOST -> AudioProfile.SPRACHKLARHEIT
                            AudioPreset.BASS_REDUCTION -> AudioProfile.HOEHEN_BOOST
                            AudioPreset.STANDARD -> AudioProfile.STANDARD
                            else -> AudioProfile.SPRACHKLARHEIT
                        }
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Fertig", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}
