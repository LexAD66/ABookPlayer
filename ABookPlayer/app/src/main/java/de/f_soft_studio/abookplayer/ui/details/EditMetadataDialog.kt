package de.f_soft_studio.abookplayer.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.AlertDialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import de.f_soft_studio.abookplayer.util.CoverHelper

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.storage.OnlineCoverScraper
import kotlinx.coroutines.launch

/**
 * EditMetadataDialog: Dialog zum Bearbeiten der Hörbuch-Metadaten inklusive
 * automatischer Online-Vervollständigung via Audible, iTunes und Google Books.
 */
@Composable
fun EditMetadataDialog(
    audiobook: Audiobook,
    onSave: (Audiobook) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(audiobook.title) }
    var author by remember { mutableStateOf(audiobook.author) }
    var narrator by remember { mutableStateOf(audiobook.narrator ?: "") }
    var parentSeries by remember { mutableStateOf(audiobook.parentSeries ?: "") }
    var series by remember { mutableStateOf(audiobook.series ?: "") }
    var seriesOrderText by remember { mutableStateOf(audiobook.seriesOrder?.toString() ?: "") }
    var description by remember { mutableStateOf(audiobook.description ?: "") }

    var showSearchPicker by remember { mutableStateOf(false) }
    var searchStatusMessage by remember { mutableStateOf<String?>(null) }
    var pendingCoverPath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (showSearchPicker) {
        val queryToSearch = if (author.isNotBlank() && !title.contains(author, ignoreCase = true)) {
            "$author $title"
        } else {
            title
        }
        MetadataSearchDialog(
            initialQuery = queryToSearch,
            onDismiss = { showSearchPicker = false },
            onSelectCandidate = { candidate ->
                var fieldsUpdated = 0
                if (!candidate.title.isNullOrBlank()) { title = candidate.title; fieldsUpdated++ }
                if (!candidate.author.isNullOrBlank()) { author = candidate.author; fieldsUpdated++ }
                if (!candidate.narrator.isNullOrBlank()) { narrator = candidate.narrator; fieldsUpdated++ }
                if (!candidate.series.isNullOrBlank()) { series = candidate.series; fieldsUpdated++ }
                if (candidate.seriesOrder != null) { seriesOrderText = candidate.seriesOrder.toString(); fieldsUpdated++ }
                if (!candidate.description.isNullOrBlank()) { description = candidate.description; fieldsUpdated++ }
                if (!candidate.coverPath.isNullOrBlank()) {
                    pendingCoverPath = candidate.coverPath
                    fieldsUpdated++
                }
                searchStatusMessage = "Übernommen: ${candidate.title} (${candidate.providerName})"
                showSearchPicker = false
            }
        )
    }

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
                // Cover Vorschau & Online Suche Button
                val currentCover = pendingCoverPath ?: audiobook.coverUri
                val coverModel = remember(currentCover, audiobook.filePath) {
                    CoverHelper.resolveCoverModel(currentCover, audiobook.filePath)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (coverModel != null) {
                        AsyncImage(
                            model = coverModel,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        FilledTonalButton(
                            onClick = { showSearchPicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Online suchen & auswählen")
                        }

                        if (!searchStatusMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = searchStatusMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }


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
                    label = { Text("Autor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = narrator,
                    onValueChange = { narrator = it },
                    label = { Text("Sprecher (Narrator)") },
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
                        narrator = narrator.trim().ifBlank { null },
                        parentSeries = parentSeries.trim().ifBlank { null },
                        series = series.trim().ifBlank { null },
                        seriesOrder = order,
                        description = description.ifBlank { null },
                        coverUri = pendingCoverPath ?: audiobook.coverUri
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
