package de.f_soft_studio.abookplayer.ui.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * LibraryScreen: Bietet die Übersicht aller Hörbücher im warmen, literarischen "Library-Look"
 * inklusive Suche, Sortierung, Status-Filter, Mini-Player Bar und Sleep-Timer Badge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    currentAudiobook: Audiobook? = null,
    isPlaying: Boolean = false,
    currentPosition: Long = 0L,
    duration: Long = 0L,
    isSleepTimerActive: Boolean = false,
    remainingTimerMs: Long = 0L,
    onTogglePlayPause: () -> Unit = {},
    onSkip10sForward: () -> Unit = {},
    onOpenPlayer: () -> Unit = {},
    onAudiobookSelected: (Audiobook) -> Unit,
    onOpenDetails: (Audiobook) -> Unit,
    onImportRequested: () -> Unit,
    onOpenStatistics: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val audiobooks by viewModel.audiobooks.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState()
    val expandedSeries by viewModel.expandedSeries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val selectedBookIds by viewModel.selectedBookIds.collectAsState()
    val favoriteBookIds by viewModel.favoriteBookIds.collectAsState()
    val isMultiSelectActive = selectedBookIds.isNotEmpty()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFromUri(it) }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hörbücher löschen?") },
            text = { Text("Möchtest du die ${selectedBookIds.size} ausgewählten Hörbücher wirklich aus der Bibliothek löschen?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteSelectedAudiobooks()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    expandedSeries?.let { stack ->
        SeriesDetailDialog(
            stack = stack,
            onDismiss = { viewModel.closeSeries() },
            onAudiobookSelected = onAudiobookSelected,
            onOpenDetails = onOpenDetails
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isMultiSelectActive) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedBookIds.size} ausgewählt",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearBookSelection() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Abbrechen")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFavoriteForSelected() }) {
                            Icon(Icons.Default.Star, contentDescription = "Favorit umschalten", tint = MaterialTheme.colorScheme.primary)
                        }
                        TextButton(onClick = { viewModel.markSelectedAsCompleted() }) {
                            Text("Beendet", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.markSelectedAsUnplayed() }) {
                            Text("Ungelesen", fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "Meine Bibliothek",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        if (isSleepTimerActive && remainingTimerMs > 0L) {
                            val remainingMins = (remainingTimerMs / 60_000L) + 1
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "⏳ $remainingMins Min",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.Search,
                                contentDescription = if (isGridView) "Listenansicht" else "Rasteransicht",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Einstellungen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onOpenStatistics) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Hörstatistiken",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            currentAudiobook?.let { book ->
                MiniPlayerBar(
                    audiobook = book,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkip10sForward = onSkip10sForward,
                    onOpenPlayer = onOpenPlayer
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("*/*"))
                    onImportRequested()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Hörbuch importieren"
                )
            }
        }
    ) { innerPadding ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Linker Steuerungs-Bereich (Sidebar)
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Suchen...", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Filter & Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = statusFilter == StatusFilter.ALLE,
                            onClick = { viewModel.onStatusFilterChanged(StatusFilter.ALLE) },
                            label = { Text("Alle") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = statusFilter == StatusFilter.FAVORITEN,
                            onClick = { viewModel.onStatusFilterChanged(StatusFilter.FAVORITEN) },
                            label = { Text("⭐ Favoriten") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = statusFilter == StatusFilter.ANGEFANGEN,
                            onClick = { viewModel.onStatusFilterChanged(StatusFilter.ANGEFANGEN) },
                            label = { Text("Angefangen") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = statusFilter == StatusFilter.BEENDET,
                            onClick = { viewModel.onStatusFilterChanged(StatusFilter.BEENDET) },
                            label = { Text("Beendet") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = statusFilter == StatusFilter.UNGESPIELT,
                            onClick = { viewModel.onStatusFilterChanged(StatusFilter.UNGESPIELT) },
                            label = { Text("Ungespielt") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }

                    Text(
                        text = "Sortierung",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = sortOrder == SortOrder.ZULETZT_GEHOERT,
                            onClick = { viewModel.onSortOrderChanged(SortOrder.ZULETZT_GEHOERT) },
                            label = { Text("Zuletzt") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = sortOrder == SortOrder.TITEL,
                            onClick = { viewModel.onSortOrderChanged(SortOrder.TITEL) },
                            label = { Text("Titel") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = sortOrder == SortOrder.AUTOR,
                            onClick = { viewModel.onSortOrderChanged(SortOrder.AUTOR) },
                            label = { Text("Autor") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = sortOrder == SortOrder.SERIEN,
                            onClick = { viewModel.onSortOrderChanged(SortOrder.SERIEN) },
                            label = { Text("Serien") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = sortOrder == SortOrder.RESTLAUFZEIT,
                            onClick = { viewModel.onSortOrderChanged(SortOrder.RESTLAUFZEIT) },
                            label = { Text("Restzeit") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                        FilterChip(
                            selected = sortOrder == SortOrder.HINZUGEFUEGT_AM,
                            onClick = { viewModel.onSortOrderChanged(SortOrder.HINZUGEFUEGT_AM) },
                            label = { Text("Neu importiert") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                }

                // Rechter Haupt-Grid-Bereich
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (libraryItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Hörbücher gefunden",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp, start = 4.dp, end = 12.dp, top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(libraryItems, key = { item ->
                                when (item) {
                                    is LibraryItem.SingleBook -> "book_${item.book.id}"
                                    is LibraryItem.Series -> "series_${item.stack.seriesName}"
                                }
                            }) { item ->
                                when (item) {
                                    is LibraryItem.SingleBook -> {
                                        val book = item.book
                                        val isSelected = selectedBookIds.contains(book.id)
                                        val isFavorite = favoriteBookIds.contains(book.id)
                                        AudiobookGridCard(
                                            audiobook = book,
                                            isSelected = isSelected,
                                            isFavorite = isFavorite,
                                            onClick = {
                                                if (isMultiSelectActive) {
                                                    viewModel.toggleBookSelection(book.id)
                                                } else {
                                                    onAudiobookSelected(book)
                                                }
                                            },
                                            onLongClick = { viewModel.toggleBookSelection(book.id) },
                                            onToggleFavorite = { viewModel.toggleFavorite(book.id) },
                                            onOpenDetails = { onOpenDetails(book) }
                                        )
                                    }
                                    is LibraryItem.Series -> {
                                        SeriesStackCard(
                                            stack = item.stack,
                                            onClick = { viewModel.openSeries(item.stack) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            // Suchleiste
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Titel, Autor oder Sprecher suchen...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suchen"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Suche leeren"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Status-Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilterChip(
                    selected = statusFilter == StatusFilter.ALLE,
                    onClick = { viewModel.onStatusFilterChanged(StatusFilter.ALLE) },
                    label = { Text("Alle") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = statusFilter == StatusFilter.FAVORITEN,
                    onClick = { viewModel.onStatusFilterChanged(StatusFilter.FAVORITEN) },
                    label = { Text("⭐ Favoriten") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = statusFilter == StatusFilter.ANGEFANGEN,
                    onClick = { viewModel.onStatusFilterChanged(StatusFilter.ANGEFANGEN) },
                    label = { Text("Angefangen") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = statusFilter == StatusFilter.BEENDET,
                    onClick = { viewModel.onStatusFilterChanged(StatusFilter.BEENDET) },
                    label = { Text("Beendet") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = statusFilter == StatusFilter.UNGESPIELT,
                    onClick = { viewModel.onStatusFilterChanged(StatusFilter.UNGESPIELT) },
                    label = { Text("Ungespielt") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            // Sortier-Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sortierung:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilterChip(
                    selected = sortOrder == SortOrder.ZULETZT_GEHOERT,
                    onClick = { viewModel.onSortOrderChanged(SortOrder.ZULETZT_GEHOERT) },
                    label = { Text("Zuletzt gehört") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = sortOrder == SortOrder.TITEL,
                    onClick = { viewModel.onSortOrderChanged(SortOrder.TITEL) },
                    label = { Text("Titel") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = sortOrder == SortOrder.AUTOR,
                    onClick = { viewModel.onSortOrderChanged(SortOrder.AUTOR) },
                    label = { Text("Autor") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = sortOrder == SortOrder.SERIEN,
                    onClick = { viewModel.onSortOrderChanged(SortOrder.SERIEN) },
                    label = { Text("Serien") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = sortOrder == SortOrder.RESTLAUFZEIT,
                    onClick = { viewModel.onSortOrderChanged(SortOrder.RESTLAUFZEIT) },
                    label = { Text("Restzeit") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                FilterChip(
                    selected = sortOrder == SortOrder.HINZUGEFUEGT_AM,
                    onClick = { viewModel.onSortOrderChanged(SortOrder.HINZUGEFUEGT_AM) },
                    label = { Text("Neu importiert") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hauptinhalt
            if (libraryItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Keine Treffer für '$searchQuery'" else "Deine Bibliothek ist noch leer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank())
                                "Keine Ergebnisse gefunden. Versuche einen anderen Suchbegriff."
                            else
                                "Kopiere .abook-Dateien in den Ordner '/Download/ABook/' oder tippe auf Scannen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.scanAudiobooks() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ordner jetzt scannen")
                        }
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(libraryItems, key = { item ->
                        when (item) {
                            is LibraryItem.SingleBook -> "book_${item.book.id}"
                            is LibraryItem.Series -> "series_${item.stack.seriesName}"
                        }
                    }) { item ->
                        when (item) {
                            is LibraryItem.SingleBook -> {
                                val book = item.book
                                val isSelected = selectedBookIds.contains(book.id)
                                val isFavorite = favoriteBookIds.contains(book.id)
                                AudiobookGridCard(
                                    audiobook = book,
                                    isSelected = isSelected,
                                    isFavorite = isFavorite,
                                    onClick = {
                                        if (isMultiSelectActive) {
                                            viewModel.toggleBookSelection(book.id)
                                        } else {
                                            onAudiobookSelected(book)
                                        }
                                    },
                                    onLongClick = { viewModel.toggleBookSelection(book.id) },
                                    onToggleFavorite = { viewModel.toggleFavorite(book.id) },
                                    onOpenDetails = { onOpenDetails(book) }
                                )
                            }
                            is LibraryItem.Series -> {
                                SeriesStackCard(
                                    stack = item.stack,
                                    onClick = { viewModel.openSeries(item.stack) }
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(libraryItems, key = { item ->
                        when (item) {
                            is LibraryItem.SingleBook -> "book_${item.book.id}"
                            is LibraryItem.Series -> "series_${item.stack.seriesName}"
                        }
                    }) { item ->
                        when (item) {
                            is LibraryItem.SingleBook -> {
                                val book = item.book
                                val isSelected = selectedBookIds.contains(book.id)
                                val isFavorite = favoriteBookIds.contains(book.id)
                                AudiobookItemCard(
                                    audiobook = book,
                                    isSelected = isSelected,
                                    isFavorite = isFavorite,
                                    onClick = {
                                        if (isMultiSelectActive) {
                                            viewModel.toggleBookSelection(book.id)
                                        } else {
                                            onAudiobookSelected(book)
                                        }
                                    },
                                    onLongClick = { viewModel.toggleBookSelection(book.id) },
                                    onToggleFavorite = { viewModel.toggleFavorite(book.id) },
                                    onOpenDetails = { onOpenDetails(book) }
                                )
                            }
                            is LibraryItem.Series -> {
                                SeriesStackCard(
                                    stack = item.stack,
                                    onClick = { viewModel.openSeries(item.stack) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun MiniPlayerBar(
    audiobook: Audiobook,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onTogglePlayPause: () -> Unit,
    onSkip10sForward: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clickable { onOpenPlayer() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val coverUri = audiobook.coverUri
                if (!coverUri.isNullOrBlank() && File(coverUri).exists()) {
                    AsyncImage(
                        model = File(coverUri),
                        contentDescription = audiobook.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = audiobook.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (audiobook.author.isNotBlank()) {
                        Text(
                            text = audiobook.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onSkip10sForward) {
                    Text(
                        text = "+10s",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudiobookItemCard(
    audiobook: Audiobook,
    isSelected: Boolean = false,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onOpenDetails: () -> Unit
) {
    val progress = if (audiobook.duration > 0) {
        (audiobook.currentPosition.toFloat() / audiobook.duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 3.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover Image oder Fallback Box
            Box {
                val coverUri = audiobook.coverUri
                if (!coverUri.isNullOrBlank() && File(coverUri).exists()) {
                    AsyncImage(
                        model = File(coverUri),
                        contentDescription = audiobook.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                if (isFavorite) {
                    Text(
                        text = "⭐",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = audiobook.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (audiobook.author.isNotBlank()) {
                    Text(
                        text = audiobook.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!audiobook.narrator.isNullOrBlank()) {
                    Text(
                        text = "Gelesen von ${audiobook.narrator}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!audiobook.series.isNullOrBlank()) {
                    Text(
                        text = if (audiobook.seriesOrder != null) "${audiobook.series} • Band ${audiobook.seriesOrder}" else audiobook.series,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$progressPercent% gehört",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatDuration(audiobook.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onOpenDetails) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Details anzeigen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudiobookGridCard(
    audiobook: Audiobook,
    isSelected: Boolean = false,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val duration = audiobook.duration
    val progress = if (duration > 0) (audiobook.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val coverUri = audiobook.coverUri
                if (!coverUri.isNullOrBlank() && File(coverUri).exists()) {
                    AsyncImage(
                        model = File(coverUri),
                        contentDescription = audiobook.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                if (isFavorite) {
                    Text(
                        text = "⭐",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Details Action Overlay Top-Right
                IconButton(
                    onClick = onOpenDetails,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Details",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Progress Bar Overlay at Bottom
                if (progress > 0f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Black.copy(alpha = 0.3f)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = audiobook.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (audiobook.author.isNotBlank()) {
                    Text(
                        text = audiobook.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0 Min"
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    return if (hours > 0) {
        "${hours} Std ${minutes} Min"
    } else {
        "${minutes} Min"
    }
}
