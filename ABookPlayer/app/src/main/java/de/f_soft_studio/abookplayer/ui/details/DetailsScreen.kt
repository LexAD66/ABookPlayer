package de.f_soft_studio.abookplayer.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.runtime.remember
import coil.compose.AsyncImage
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Chapter
import de.f_soft_studio.abookplayer.util.CoverHelper
import java.io.File
import java.util.Locale

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import de.f_soft_studio.abookplayer.domain.model.ExportState

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Image

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * DetailsScreen: Zeigt ausführliche Metadaten, Beschreibung, Dateigröße und Kapitelstatistik eines Hörbuchs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    audiobook: Audiobook?,
    chapters: List<Chapter>,
    exportState: ExportState = ExportState.Idle,
    isSearchingOnline: Boolean = false,
    onPlayClick: () -> Unit,
    onExportToUri: (android.net.Uri) -> Unit = {},
    onSearchCoverOnline: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onOpenCharacters: () -> Unit = {},
    onOpenInfo: (Audiobook) -> Unit = {},
    onUpdateMetadata: (Audiobook) -> Unit = {},
    onBackClick: () -> Unit
) {

    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val coversDir = de.f_soft_studio.abookplayer.storage.LibraryLocationManager.getCoversDir(context)
            val targetFile = File(coversDir, "custom_cover_${audiobook?.id ?: 0}.jpg")
            try {

                context.contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output -> input.copyTo(output) }
                }
                if (audiobook != null) {
                    onUpdateMetadata(audiobook.copy(coverUri = targetFile.absolutePath))
                }
            } catch (e: Exception) {
                Log.e("DetailsScreen", "Fehler beim Speichern des Galerie-Covers: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hörbuch-Details", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    audiobook?.let { book ->
                        IconButton(onClick = { onOpenInfo(book) }) {
                            Icon(Icons.Default.Info, contentDescription = "eBook & Hörbuch Info", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Metadaten bearbeiten", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (audiobook != null && showEditDialog) {
            EditMetadataDialog(
                audiobook = audiobook,
                onSave = { updatedBook -> onUpdateMetadata(updatedBook) },
                onDismiss = { showEditDialog = false }
            )
        }
        if (audiobook == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Keine Daten verfügbar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val file = File(audiobook.filePath)
            val fileSize = if (file.exists()) {
                if (file.isDirectory) {
                    file.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                } else {
                    file.length()
                }
            } else 0L
            val fileSizeFormatted = formatFileSize(fileSize)
            val effectiveDuration = if (audiobook.duration > 0L) audiobook.duration else chapters.sumOf { 0L }

            val createDocumentLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/zip")
            ) { uri ->
                if (uri != null) {
                    onExportToUri(uri)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Cover Artwork
                val coverModel = remember(audiobook.id, audiobook.coverUri, audiobook.filePath) {
                    CoverHelper.resolveCoverModel(audiobook.coverUri, audiobook.filePath)
                }
                if (coverModel != null) {
                    AsyncImage(
                        model = coverModel,
                        contentDescription = audiobook.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Author
                Text(
                    text = audiobook.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (audiobook.author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = audiobook.author,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!audiobook.series.isNullOrBlank() || !audiobook.parentSeries.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val seriesText = buildString {
                        if (!audiobook.parentSeries.isNullOrBlank()) {
                            append("Reihe: ${audiobook.parentSeries}")
                        }
                        if (!audiobook.series.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("Serie: ${audiobook.series}")
                        }
                        if (audiobook.seriesOrder != null) {
                            append(" (Band ${audiobook.seriesOrder})")
                        }
                    }
                    Text(
                        text = seriesText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }


                Spacer(modifier = Modifier.height(12.dp))

                // Summary Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("⏱️ ${formatTimeMs(effectiveDuration)}") }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("📚 ${chapters.size} Kapitel") }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("💾 $fileSizeFormatted") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Play / Resume Action Button
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (audiobook.currentPosition > 0) "Wiedergabe fortsetzen" else "Wiedergabe starten",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Export Button & Online Scraper Button
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        val sanitizedTitle = audiobook.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                        createDocumentLauncher.launch("$sanitizedTitle.abook")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = exportState !is ExportState.Exporting,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (exportState is ExportState.Exporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportiere... (${exportState.progressPercent}%)")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Als .abook exportieren", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSearchCoverOnline,
                            modifier = Modifier.weight(1f),
                            enabled = !isSearchingOnline,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSearchingOnline) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Online-Cover", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            }
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galerie-Cover", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenCharacters,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Figuren", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bearbeiten", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }
                    }
                }


                if (exportState is ExportState.Success) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exportState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (exportState is ExportState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exportState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description Section Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Beschreibung",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = audiobook.description.takeIf { !it.isNullOrBlank() }
                                ?: "Keine Beschreibung in metadata.json vorhanden.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Technical Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Datei & Statistik",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailStatRow(icon = Icons.AutoMirrored.Filled.List, label = "Kapitelanzahl", value = "${chapters.size} Kapitel")
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailStatRow(icon = Icons.Default.Schedule, label = "Gesamtdauer", value = formatTimeMs(audiobook.duration))
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailStatRow(icon = Icons.Default.Folder, label = "Dateigröße", value = fileSizeFormatted)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailStatRow(icon = Icons.Default.Folder, label = "Dateipfad", value = audiobook.filePath)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailStatRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.4f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

private fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val kb = sizeBytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.getDefault(), "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
        else -> String.format(Locale.getDefault(), "%.0f KB", kb)
    }
}

private fun formatTimeMs(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
