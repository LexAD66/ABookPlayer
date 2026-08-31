package de.f_soft_studio.abookplayer.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.f_soft_studio.abookplayer.domain.model.Audiobook

/**
 * Dialog zum manuellen Bearbeiten von Hörbuch-Metadaten (Titel, Autor, Sprecher, Serienname & Band).
 */
@Composable
fun EditAudiobookDialog(
    audiobook: Audiobook,
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, narrator: String?, parentSeries: String?, series: String?, seriesOrder: Int?) -> Unit
) {
    var title by remember { mutableStateOf(audiobook.title) }
    var author by remember { mutableStateOf(audiobook.author) }
    var narrator by remember { mutableStateOf(audiobook.narrator ?: "") }
    var parentSeries by remember { mutableStateOf(audiobook.parentSeries ?: "") }
    var series by remember { mutableStateOf(audiobook.series ?: "") }
    var seriesOrderText by remember { mutableStateOf(audiobook.seriesOrder?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hörbuch bearbeiten", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Autor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = narrator,
                    onValueChange = { narrator = it },
                    label = { Text("Sprecher") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = parentSeries,
                    onValueChange = { parentSeries = it },
                    label = { Text("Übergeordnete Reihe (z. B. Perry Rhodan)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = series,
                    onValueChange = { series = it },
                    label = { Text("Serie / Zyklus (z. B. Atlantis)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = seriesOrderText,
                    onValueChange = { seriesOrderText = it },
                    label = { Text("Band-Nummer (z. B. 1, 2, 3)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalParentSeries = parentSeries.trim().ifBlank { null }
                    val finalSeries = series.trim().ifBlank { null }
                    val finalSeriesOrder = seriesOrderText.trim().toIntOrNull()
                    val finalNarrator = narrator.trim().ifBlank { null }
                    onConfirm(title.trim(), author.trim(), finalNarrator, finalParentSeries, finalSeries, finalSeriesOrder)
                }
            ) {
                Text("Speichern")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
