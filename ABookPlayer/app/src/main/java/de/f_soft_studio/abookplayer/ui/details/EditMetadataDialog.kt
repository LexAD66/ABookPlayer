package de.f_soft_studio.abookplayer.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.f_soft_studio.abookplayer.domain.model.Audiobook

/**
 * EditMetadataDialog: Dialog zum Bearbeiten der Hörbuch-Metadaten.
 */
@Composable
fun EditMetadataDialog(
    audiobook: Audiobook,
    onSave: (Audiobook) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(audiobook.title) }
    var author by remember { mutableStateOf(audiobook.author) }
    var parentSeries by remember { mutableStateOf(audiobook.parentSeries ?: "") }
    var series by remember { mutableStateOf(audiobook.series ?: "") }
    var seriesOrderText by remember { mutableStateOf(audiobook.seriesOrder?.toString() ?: "") }
    var description by remember { mutableStateOf(audiobook.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Metadaten bearbeiten",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Autor / Sprecher") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = parentSeries,
                    onValueChange = { parentSeries = it },
                    label = { Text("Übergeordnete Reihe / Universum (z. B. Perry Rhodan)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = series,
                        onValueChange = { series = it },
                        label = { Text("Serie / Zyklus (z. B. Atlantis)") },
                        modifier = Modifier.weight(0.65f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = seriesOrderText,
                        onValueChange = { seriesOrderText = it },
                        label = { Text("Band #") },
                        modifier = Modifier.weight(0.35f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung / Klappentext") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val order = seriesOrderText.toIntOrNull()
                    val updated = audiobook.copy(
                        title = title.ifBlank { audiobook.title },
                        author = author,
                        parentSeries = parentSeries.trim().ifBlank { null },
                        series = series.trim().ifBlank { null },
                        seriesOrder = order,
                        description = description.ifBlank { null }
                    )
                    onSave(updated)
                    onDismiss()
                },

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Speichern", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
