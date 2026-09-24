package de.f_soft_studio.abookplayer.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import de.f_soft_studio.abookplayer.storage.OnlineCoverScraper
import de.f_soft_studio.abookplayer.storage.OnlineMetadataResult
import kotlinx.coroutines.launch

/**
 * Dialog zur Online-Suche und manuellen Auswahl des passenden Hörbuch-Treffers inklusive Cover-Vorschau.
 */
@Composable
fun MetadataSearchDialog(
    initialQuery: String,
    onDismiss: () -> Unit,
    onSelectCandidate: (OnlineMetadataResult) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scraper = remember { OnlineCoverScraper(context) }

    var searchQuery by remember { mutableStateOf(scraper.cleanQueryText(initialQuery)) }
    var candidates by remember { mutableStateOf<List<OnlineMetadataResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isDownloadingSelection by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    fun performSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        isSearching = true
        searchError = null
        coroutineScope.launch {
            try {
                val results = scraper.searchCandidatesForQuery(q, limit = 12)
                candidates = results
                if (results.isEmpty()) {
                    searchError = "Keine Treffer gefunden. Bitte Suchbegriff anpassen (z. B. nur Buchtitel oder Autor)."
                }
            } catch (e: Exception) {
                searchError = "Fehler bei der Online-Suche: ${e.message}"
            } finally {
                isSearching = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (searchQuery.isNotBlank()) {
            performSearch(searchQuery)
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isDownloadingSelection) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(640.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Passendes Hörbuch auswählen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Suchfeld
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Suchbegriff (Audible / iTunes)") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Löschen")
                                }
                            }
                            IconButton(onClick = { performSearch(searchQuery) }) {
                                Icon(Icons.Default.Search, contentDescription = "Suchen", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Ladezustand / Fehlermeldung
                if (isSearching) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Suche bei Audible & iTunes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (isDownloadingSelection) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Lade Metadaten & HD-Cover...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (searchError != null) {
                    Text(
                        text = searchError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                // Trefferliste
                if (!isSearching && candidates.isNotEmpty()) {
                    Text(
                        text = "${candidates.size} Treffer gefunden – Bitte antippen zum Übernehmen:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(candidates) { candidate ->
                            CandidateCard(
                                candidate = candidate,
                                onClick = {
                                    isDownloadingSelection = true
                                    coroutineScope.launch {
                                        try {
                                            var finalCandidate = candidate
                                            if (!candidate.coverUrl.isNullOrBlank()) {
                                                val downloadedPath = scraper.downloadSelectedCover(
                                                    candidate.coverUrl,
                                                    candidate.title ?: "cover"
                                                )
                                                if (downloadedPath != null) {
                                                    finalCandidate = candidate.copy(coverPath = downloadedPath)
                                                }
                                            }
                                            onSelectCandidate(finalCandidate)
                                        } finally {
                                            isDownloadingSelection = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDownloadingSelection
            ) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
private fun CandidateCard(
    candidate: OnlineMetadataResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover Vorschau
            if (!candidate.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = candidate.coverUrl,
                    contentDescription = candidate.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadaten
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = candidate.title ?: "Unbekannter Titel",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!candidate.author.isNullOrBlank()) {
                    Text(
                        text = "Autor: ${candidate.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!candidate.narrator.isNullOrBlank()) {
                    Text(
                        text = "Sprecher: ${candidate.narrator}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!candidate.series.isNullOrBlank()) {
                    val orderSuffix = if (candidate.seriesOrder != null) " (Band ${candidate.seriesOrder})" else ""
                    Text(
                        text = "Reihe: ${candidate.series}$orderSuffix",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Provider Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (candidate.providerName.contains("Audible"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = candidate.providerName,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = if (candidate.providerName.contains("Audible"))
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
