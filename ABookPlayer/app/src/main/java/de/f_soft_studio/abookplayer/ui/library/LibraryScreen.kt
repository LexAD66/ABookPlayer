package de.f_soft_studio.abookplayer.ui.library

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.ui.library.components.AddAudiobookBottomSheet
import de.f_soft_studio.abookplayer.ui.library.components.AudiobookGridCard
import de.f_soft_studio.abookplayer.ui.library.components.AudiobookItemCard
import de.f_soft_studio.abookplayer.ui.library.components.FilterBottomSheet
import de.f_soft_studio.abookplayer.ui.library.components.MiniPlayerBar
import de.f_soft_studio.abookplayer.ui.library.components.SeriesCarouselCard
import de.f_soft_studio.abookplayer.ui.library.components.SeriesFolderListCard

/**
 * LibraryScreen: Bietet die Übersicht aller Hörbücher im warmen, literarischen "Library-Look".
 * Redesign: Fokussiert auf den Hörfluss; Wartung/Scan in 'Bibliothek verwalten' ausgelagert,
 * Filter und Import in leicht zugänglichen Bottom Sheets.
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
    onImportRequested: () -> Unit = {},
    onOpenLibraryManagement: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val libraryItems by viewModel.libraryItems.collectAsState()
    val expandedSeries by viewModel.expandedSeries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val seriesDisplayMode by viewModel.seriesDisplayMode.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val selectedBookIds by viewModel.selectedBookIds.collectAsState()
    val favoriteBookIds by viewModel.favoriteBookIds.collectAsState()
    val maintenanceState by viewModel.maintenanceState.collectAsState()
    val isMultiSelectActive = selectedBookIds.isNotEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var isSearchFieldVisible by remember { mutableStateOf(false) }

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

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.importFromFolderUri(it) }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Dialoge & BottomSheets
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

    if (showFilterBottomSheet) {
        FilterBottomSheet(
            statusFilter = statusFilter,
            sortOrder = sortOrder,
            seriesDisplayMode = seriesDisplayMode,
            onStatusFilterChanged = { viewModel.onStatusFilterChanged(it) },
            onSortOrderChanged = { viewModel.onSortOrderChanged(it) },
            onSeriesDisplayModeChanged = { viewModel.setSeriesDisplayMode(it) },
            onResetFilters = {
                viewModel.onStatusFilterChanged(StatusFilter.ALLE)
                viewModel.onSortOrderChanged(SortOrder.ZULETZT_GEHOERT)
                viewModel.onSearchQueryChanged("")
            },
            onDismiss = { showFilterBottomSheet = false }
        )
    }

    if (showAddBottomSheet) {
        AddAudiobookBottomSheet(
            onImportFolder = { folderPickerLauncher.launch(null) },
            onImportFile = { filePickerLauncher.launch(arrayOf("*/*")) },
            onScanDefaultFolder = { viewModel.scanAudiobooks() },
            onDismiss = { showAddBottomSheet = false }
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

    val isAnyFilterActive = statusFilter != StatusFilter.ALLE ||
            sortOrder != SortOrder.ZULETZT_GEHOERT ||
            searchQuery.isNotBlank()

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
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Favorit umschalten",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(onClick = { viewModel.markSelectedAsCompleted() }) {
                            Text("Beendet", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.markSelectedAsUnplayed() }) {
                            Text("Ungelesen", fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Löschen",
                                tint = MaterialTheme.colorScheme.error
                            )
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

                        IconButton(onClick = {
                            isSearchFieldVisible = !isSearchFieldVisible
                            if (!isSearchFieldVisible && searchQuery.isNotBlank()) {
                                viewModel.onSearchQueryChanged("")
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Suche umschalten",
                                tint = if (isSearchFieldVisible || searchQuery.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                contentDescription = if (isGridView) "Listenansicht" else "Rasteransicht",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box {
                            IconButton(onClick = { showOverflowMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Weitere Optionen",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Bibliothek verwalten") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Build, contentDescription = null)
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenLibraryManagement()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Hörstatistiken") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Info, contentDescription = null)
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenStatistics()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Einstellungen") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Settings, contentDescription = null)
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenSettings()
                                    }
                                )
                            }
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
                onClick = { showAddBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Hörbücher hinzufügen"
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
                // Linke Steuerungs-Sidebar
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
                        text = "Status",
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

                // Rechter Inhaltsbereich
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    LibraryContent(
                        libraryItems = libraryItems,
                        isGridView = true,
                        searchQuery = searchQuery,
                        isAnyFilterActive = isAnyFilterActive,
                        selectedBookIds = selectedBookIds,
                        favoriteBookIds = favoriteBookIds,
                        currentAudiobook = currentAudiobook,
                        isMultiSelectActive = isMultiSelectActive,
                        seriesDisplayMode = seriesDisplayMode,
                        isScanning = maintenanceState.isScanning,
                        scanProgressText = maintenanceState.scanProgressText,
                        onAudiobookSelected = onAudiobookSelected,
                        onOpenDetails = onOpenDetails,
                        onResetFilters = {
                            viewModel.onStatusFilterChanged(StatusFilter.ALLE)
                            viewModel.onSortOrderChanged(SortOrder.ZULETZT_GEHOERT)
                            viewModel.onSearchQueryChanged("")
                        },
                        onOpenAddSheet = { showAddBottomSheet = true },
                        onToggleBookSelection = { viewModel.toggleBookSelection(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onOpenSeries = { viewModel.openSeries(it) }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Linearer Ladebalken während Scan-Aktivität
                if (maintenanceState.isScanning) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                // Sichtbares Scan-Statusbanner
                AnimatedVisibility(
                    visible = maintenanceState.isScanning,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = maintenanceState.scanProgressText ?: "Bibliothek wird gescannt...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Dateien und Kapitel werden analysiert...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Suchfeld (wenn aktiv ausgeklappt oder Suchtext vorhanden)
                if (isSearchFieldVisible || searchQuery.isNotBlank()) {
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
                }

                // Kompakte Active-Filter-Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter-Button mit Icon
                    AssistChip(
                        onClick = { showFilterBottomSheet = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                text = "Filter & Sortierung",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    )

                    // Aktiver Status-Chip
                    if (statusFilter != StatusFilter.ALLE) {
                        FilterChip(
                            selected = true,
                            onClick = { showFilterBottomSheet = true },
                            label = {
                                Text(
                                    when (statusFilter) {
                                        StatusFilter.FAVORITEN -> "⭐ Favoriten"
                                        StatusFilter.ANGEFANGEN -> "Angefangen"
                                        StatusFilter.BEENDET -> "Beendet"
                                        StatusFilter.UNGESPIELT -> "Ungespielt"
                                        StatusFilter.ALLE -> "Alle"
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    // Aktiver Sortier-Chip
                    FilterChip(
                        selected = true,
                        onClick = { showFilterBottomSheet = true },
                        label = {
                            Text(
                                when (sortOrder) {
                                    SortOrder.ZULETZT_GEHOERT -> "Zuletzt gehört"
                                    SortOrder.TITEL -> "Titel"
                                    SortOrder.AUTOR -> "Autor"
                                    SortOrder.SERIEN -> "Serien"
                                    SortOrder.RESTLAUFZEIT -> "Restzeit"
                                    SortOrder.HINZUGEFUEGT_AM -> "Neu importiert"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // Zurücksetzen-Button wenn Filter aktiv sind
                    if (isAnyFilterActive) {
                        TextButton(
                            onClick = {
                                viewModel.onStatusFilterChanged(StatusFilter.ALLE)
                                viewModel.onSortOrderChanged(SortOrder.ZULETZT_GEHOERT)
                                viewModel.onSearchQueryChanged("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Zurücksetzen")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hauptinhalt
                LibraryContent(
                    libraryItems = libraryItems,
                    isGridView = isGridView,
                    searchQuery = searchQuery,
                    isAnyFilterActive = isAnyFilterActive,
                    selectedBookIds = selectedBookIds,
                    favoriteBookIds = favoriteBookIds,
                    currentAudiobook = currentAudiobook,
                    isMultiSelectActive = isMultiSelectActive,
                    seriesDisplayMode = seriesDisplayMode,
                    isScanning = maintenanceState.isScanning,
                    scanProgressText = maintenanceState.scanProgressText,
                    onAudiobookSelected = onAudiobookSelected,
                    onOpenDetails = onOpenDetails,
                    onResetFilters = {
                        viewModel.onStatusFilterChanged(StatusFilter.ALLE)
                        viewModel.onSortOrderChanged(SortOrder.ZULETZT_GEHOERT)
                        viewModel.onSearchQueryChanged("")
                    },
                    onOpenAddSheet = { showAddBottomSheet = true },
                    onToggleBookSelection = { viewModel.toggleBookSelection(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenSeries = { viewModel.openSeries(it) }
                )
            }
        }
    }
}

@Composable
private fun LibraryContent(
    libraryItems: List<LibraryItem>,
    isGridView: Boolean,
    searchQuery: String,
    isAnyFilterActive: Boolean,
    selectedBookIds: Set<Long>,
    favoriteBookIds: Set<Long>,
    currentAudiobook: Audiobook?,
    isMultiSelectActive: Boolean,
    seriesDisplayMode: SeriesDisplayMode,
    isScanning: Boolean = false,
    scanProgressText: String? = null,
    onAudiobookSelected: (Audiobook) -> Unit,
    onOpenDetails: (Audiobook) -> Unit,
    onResetFilters: () -> Unit,
    onOpenAddSheet: () -> Unit,
    onToggleBookSelection: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onOpenSeries: (SeriesStack) -> Unit
) {
    if (libraryItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = scanProgressText ?: "Bibliothek wird gescannt...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hörbücher werden gesucht & analysiert. Bitte einen Moment Geduld...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
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
                        text = if (searchQuery.isNotBlank()) {
                            "Keine Treffer für '$searchQuery'"
                        } else if (isAnyFilterActive) {
                            "Keine Hörbücher für diesen Filter"
                        } else {
                            "Deine Bibliothek ist noch leer"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAnyFilterActive) {
                            "Versuche deine Filterkriterien anzupassen oder zurückzusetzen."
                        } else {
                            "Importiere Ordner oder Dateien (.abook, .zip, .m4b) über den Hinzufügen-Button."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    if (isAnyFilterActive) {
                        OutlinedButton(
                            onClick = onResetFilters,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Filter zurücksetzen")
                        }
                    } else {
                        Button(
                            onClick = onOpenAddSheet,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hörbücher hinzufügen")
                        }
                    }
                }
            }
        }
    } else if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = libraryItems,
                key = { item ->
                    when (item) {
                        is LibraryItem.SingleBook -> "book_${item.book.id}"
                        is LibraryItem.Series -> "series_${item.stack.author}_${item.stack.seriesName}"
                    }
                },
                span = { item ->
                    when (item) {
                        is LibraryItem.SingleBook -> GridItemSpan(1)
                        is LibraryItem.Series -> {
                            when (seriesDisplayMode) {
                                SeriesDisplayMode.STAPEL_KARTE -> GridItemSpan(1)
                                SeriesDisplayMode.REIHEN_KARUSSELL,
                                SeriesDisplayMode.ORDNER_LISTE -> GridItemSpan(maxLineSpan)
                            }
                        }
                    }
                }
            ) { item ->
                when (item) {
                    is LibraryItem.SingleBook -> {
                        val book = item.book
                        val isSelected = selectedBookIds.contains(book.id)
                        val isFavorite = favoriteBookIds.contains(book.id)
                        val isCurrentlyPlaying = (currentAudiobook?.id == book.id)
                        AudiobookGridCard(
                            audiobook = book,
                            isSelected = isSelected,
                            isFavorite = isFavorite,
                            isCurrentlyPlaying = isCurrentlyPlaying,
                            onClick = {
                                if (isMultiSelectActive) {
                                    onToggleBookSelection(book.id)
                                } else {
                                    onAudiobookSelected(book)
                                }
                            },
                            onLongClick = { onToggleBookSelection(book.id) },
                            onToggleFavorite = { onToggleFavorite(book.id) },
                            onOpenDetails = { onOpenDetails(book) }
                        )
                    }
                    is LibraryItem.Series -> {
                        when (seriesDisplayMode) {
                            SeriesDisplayMode.STAPEL_KARTE -> {
                                SeriesStackCard(
                                    stack = item.stack,
                                    onClick = { onOpenSeries(item.stack) }
                                )
                            }
                            SeriesDisplayMode.REIHEN_KARUSSELL -> {
                                SeriesCarouselCard(
                                    stack = item.stack,
                                    onBookClick = onAudiobookSelected,
                                    onOpenSeries = { onOpenSeries(item.stack) }
                                )
                            }
                            SeriesDisplayMode.ORDNER_LISTE -> {
                                SeriesFolderListCard(
                                    stack = item.stack,
                                    onClick = { onOpenSeries(item.stack) }
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(libraryItems, key = { item ->
                when (item) {
                    is LibraryItem.SingleBook -> "book_${item.book.id}"
                    is LibraryItem.Series -> "series_${item.stack.author}_${item.stack.seriesName}"
                }
            }) { item ->
                when (item) {
                    is LibraryItem.SingleBook -> {
                        val book = item.book
                        val isSelected = selectedBookIds.contains(book.id)
                        val isFavorite = favoriteBookIds.contains(book.id)
                        val isCurrentlyPlaying = (currentAudiobook?.id == book.id)
                        AudiobookItemCard(
                            audiobook = book,
                            isSelected = isSelected,
                            isFavorite = isFavorite,
                            isCurrentlyPlaying = isCurrentlyPlaying,
                            onClick = {
                                if (isMultiSelectActive) {
                                    onToggleBookSelection(book.id)
                                } else {
                                    onAudiobookSelected(book)
                                }
                            },
                            onLongClick = { onToggleBookSelection(book.id) },
                            onToggleFavorite = { onToggleFavorite(book.id) },
                            onOpenDetails = { onOpenDetails(book) }
                        )
                    }
                    is LibraryItem.Series -> {
                        when (seriesDisplayMode) {
                            SeriesDisplayMode.STAPEL_KARTE -> {
                                SeriesStackCard(
                                    stack = item.stack,
                                    onClick = { onOpenSeries(item.stack) }
                                )
                            }
                            SeriesDisplayMode.REIHEN_KARUSSELL -> {
                                SeriesCarouselCard(
                                    stack = item.stack,
                                    onBookClick = onAudiobookSelected,
                                    onOpenSeries = { onOpenSeries(item.stack) }
                                )
                            }
                            SeriesDisplayMode.ORDNER_LISTE -> {
                                SeriesFolderListCard(
                                    stack = item.stack,
                                    onClick = { onOpenSeries(item.stack) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
