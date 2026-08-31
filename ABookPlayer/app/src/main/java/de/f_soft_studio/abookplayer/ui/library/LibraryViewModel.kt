package de.f_soft_studio.abookplayer.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.storage.AbookStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Sortier-Optionen für die Hörbuch-Bibliothek.
 */
enum class SortOrder {
    ZULETZT_GEHOERT,
    TITEL,
    AUTOR,
    SERIEN,
    RESTLAUFZEIT,
    HINZUGEFUEGT_AM
}

/**
 * Status-Filter für die Hörbuch-Bibliothek.
 */
enum class StatusFilter {
    ALLE,
    ANGEFANGEN,
    BEENDET,
    UNGESPIELT,
    FAVORITEN
}

/**
 * Darstellungsmodi für Buchreihen / Unterordner.
 */
enum class SeriesDisplayMode(val label: String) {
    STAPEL_KARTE("Kompakter Stapel"),
    REIHEN_KARUSSELL("Regal / Karussell"),
    ORDNER_LISTE("Ordner-Struktur")
}

/**
 * Repräsentiert eine gebündelte Buchreihe in der Bibliothek.
 */
data class SeriesStack(
    val seriesName: String,
    val author: String,
    val books: List<Audiobook>,
    val totalDuration: Long,
    val totalProgress: Float,
    val coverUri: String?,
    val parentSeries: String? = null
)

/**
 * Ein Bibliothekselement: Entweder ein Einzelbuch ODER ein Serien-Stapel.
 */
sealed class LibraryItem {
    data class SingleBook(val book: Audiobook) : LibraryItem()
    data class Series(val stack: SeriesStack) : LibraryItem()
}

/**
 * ViewModel zur Bereitstellung der Hörbuchbibliothek, Filterung, Suche und Import.
 */
class LibraryViewModel(
    private val repository: AudiobookRepository,
    private val storage: AbookStorage,
    private val context: android.content.Context? = null
) : ViewModel() {

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.ZULETZT_GEHOERT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _statusFilter = MutableStateFlow(StatusFilter.ALLE)
    val statusFilter: StateFlow<StatusFilter> = _statusFilter.asStateFlow()

    private val _seriesDisplayMode = MutableStateFlow(SeriesDisplayMode.STAPEL_KARTE)
    val seriesDisplayMode: StateFlow<SeriesDisplayMode> = _seriesDisplayMode.asStateFlow()

    fun setSeriesDisplayMode(mode: SeriesDisplayMode) {
        _seriesDisplayMode.value = mode
    }

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _selectedBookIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedBookIds: StateFlow<Set<Long>> = _selectedBookIds.asStateFlow()

    val favoriteBookIds: StateFlow<Set<Long>> = repository.getAllAudiobooks()
        .map { books -> books.filter { it.isFavorite }.map { it.id }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptySet()
        )

    private val _expandedSeries = MutableStateFlow<SeriesStack?>(null)
    val expandedSeries: StateFlow<SeriesStack?> = _expandedSeries.asStateFlow()

    fun openSeries(stack: SeriesStack) {
        _expandedSeries.value = stack
    }

    fun closeSeries() {
        _expandedSeries.value = null
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun toggleBookSelection(id: Long) {
        val current = _selectedBookIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedBookIds.value = current
    }

    fun clearBookSelection() {
        _selectedBookIds.value = emptySet()
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            val book = repository.getAudiobookById(id)
            book?.let {
                repository.toggleFavorite(id, !it.isFavorite)
            }
        }
    }

    fun toggleFavoriteForSelected() {
        viewModelScope.launch {
            val selected = _selectedBookIds.value
            if (selected.isEmpty()) return@launch
            val books = repository.getAllAudiobooks().first().filter { selected.contains(it.id) }
            val allSelectedAreFavs = books.all { it.isFavorite }
            books.forEach { book ->
                repository.toggleFavorite(book.id, !allSelectedAreFavs)
            }
            _selectedBookIds.value = emptySet()
        }
    }

    fun deleteSelectedAudiobooks() {
        viewModelScope.launch {
            val ids = _selectedBookIds.value
            if (ids.isEmpty()) return@launch
            ids.forEach { repository.deleteAudiobook(it) }
            _selectedBookIds.value = emptySet()
            _messageEvent.emit("${ids.size} Hörbuch(er) gelöscht")
        }
    }

    fun markSelectedAsCompleted() {
        viewModelScope.launch {
            val ids = _selectedBookIds.value
            ids.forEach { id ->
                val book = repository.getAudiobookById(id)
                book?.let {
                    val completedPos = if (it.duration > 0L) it.duration else 1L
                    repository.saveAudiobook(it.copy(currentPosition = completedPos))
                }
            }
            _selectedBookIds.value = emptySet()
            _messageEvent.emit("${ids.size} Hörbuch(er) als beendet markiert")
        }
    }

    fun markSelectedAsUnplayed() {
        viewModelScope.launch {
            val ids = _selectedBookIds.value
            ids.forEach { id ->
                val book = repository.getAudiobookById(id)
                book?.let {
                    repository.saveAudiobook(it.copy(currentPosition = 0L))
                }
            }
            _selectedBookIds.value = emptySet()
            _messageEvent.emit("${ids.size} Hörbuch(er) als ungelesen zurückgesetzt")
        }
    }

    val audiobooks: StateFlow<List<Audiobook>> = combine(
        repository.getAllAudiobooks(),
        _searchQuery,
        _sortOrder,
        _statusFilter
    ) { books, query, sort, status ->
        val searchFiltered = if (query.isBlank()) {
            books
        } else {
            books.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true) ||
                (it.parentSeries?.contains(query, ignoreCase = true) == true) ||
                (it.series?.contains(query, ignoreCase = true) == true) ||
                (it.narrator?.contains(query, ignoreCase = true) == true)
            }
        }

        val statusFiltered = when (status) {
            StatusFilter.ALLE -> searchFiltered
            StatusFilter.ANGEFANGEN -> searchFiltered.filter { it.currentPosition > 0L && (it.duration <= 0L || it.currentPosition < it.duration - 5000L) }
            StatusFilter.BEENDET -> searchFiltered.filter { (it.duration > 0L && it.currentPosition >= it.duration - 5000L) || (it.duration <= 0L && it.currentPosition > 0L) }
            StatusFilter.UNGESPIELT -> searchFiltered.filter { it.currentPosition == 0L }
            StatusFilter.FAVORITEN -> searchFiltered.filter { it.isFavorite }
        }

        when (sort) {
            SortOrder.ZULETZT_GEHOERT -> statusFiltered.sortedByDescending { it.lastPlayed }
            SortOrder.TITEL -> statusFiltered.sortedBy { it.title.lowercase() }
            SortOrder.AUTOR -> statusFiltered.sortedBy { it.author.lowercase() }
            SortOrder.SERIEN -> statusFiltered.sortedWith(
                compareBy<Audiobook> { it.parentSeries?.lowercase() ?: it.series?.lowercase() ?: "zzzz" }
                    .thenBy { it.series?.lowercase() ?: "zzzz" }
                    .thenBy { it.seriesOrder ?: 999 }
                    .thenBy { it.title.lowercase() }
            )
            SortOrder.RESTLAUFZEIT -> statusFiltered.sortedBy { maxOf(0L, it.duration - it.currentPosition) }
            SortOrder.HINZUGEFUEGT_AM -> statusFiltered.sortedByDescending { it.addedAt }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val libraryItems: StateFlow<List<LibraryItem>> = combine(
        audiobooks,
        _sortOrder
    ) { books, sort ->
        if (sort == SortOrder.SERIEN) {
            val (seriesBooks, standaloneBooks) = books.partition { !it.series.isNullOrBlank() || !it.parentSeries.isNullOrBlank() }
            val seriesGroups = seriesBooks.groupBy {
                val p = it.parentSeries?.trim().orEmpty()
                val s = it.series?.trim().orEmpty()
                if (p.isNotBlank() && s.isNotBlank()) "$p - $s" else if (p.isNotBlank()) p else s
            }

            val items = mutableListOf<LibraryItem>()

            seriesGroups.forEach { (seriesDisplayName, group) ->
                val sortedGroup = group.sortedWith(
                    compareBy<Audiobook> { it.seriesOrder ?: 999 }.thenBy { it.title.lowercase() }
                )
                if (sortedGroup.size >= 2) {
                    val author = sortedGroup.firstOrNull { it.author.isNotBlank() }?.author ?: ""
                    val totalDuration = sortedGroup.sumOf { it.duration }
                    val totalPosition = sortedGroup.sumOf { it.currentPosition }
                    val totalProgress = if (totalDuration > 0) (totalPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f
                    val cover = sortedGroup.firstOrNull { !it.coverUri.isNullOrBlank() }?.coverUri
                    val parent = sortedGroup.firstOrNull { !it.parentSeries.isNullOrBlank() }?.parentSeries
                    items.add(
                        LibraryItem.Series(
                            SeriesStack(
                                seriesName = seriesDisplayName,
                                author = author,
                                books = sortedGroup,
                                totalDuration = totalDuration,
                                totalProgress = totalProgress,
                                coverUri = cover,
                                parentSeries = parent
                            )
                        )
                    )
                } else {
                    sortedGroup.forEach { items.add(LibraryItem.SingleBook(it)) }
                }
            }

            standaloneBooks.forEach { items.add(LibraryItem.SingleBook(it)) }
            items
        } else {
            books.map { LibraryItem.SingleBook(it) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChanged(order: SortOrder) {
        _sortOrder.value = order
    }

    fun onStatusFilterChanged(filter: StatusFilter) {
        _statusFilter.value = filter
    }

    private val _maintenanceState = MutableStateFlow(LibraryMaintenanceUiState())
    val maintenanceState: StateFlow<LibraryMaintenanceUiState> = _maintenanceState.asStateFlow()

    /** Abgeleitete View für bestehende Consumer (LibraryScreen). */
    val detectedDuplicates: StateFlow<List<de.f_soft_studio.abookplayer.util.DuplicateMatch>> =
        _maintenanceState
            .map { it.duplicates }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun scanAudiobooks() {
        viewModelScope.launch {
            _maintenanceState.value = _maintenanceState.value.copy(isScanning = true)
            try {
                storage.clearDetectedDuplicates()
                val imported = storage.scanAndImport()
                val duplicates = storage.getDetectedDuplicates()
                val message = when {
                    imported.isNotEmpty() -> "${imported.size} neue(s) Hörbuch(er) importiert"
                    duplicates.isNotEmpty() -> "${duplicates.size} identische(s) Duplikat(e) gefunden"
                    else -> "Scan beendet. Keine neuen Hörbücher oder Ordner gefunden."
                }
                _maintenanceState.value = _maintenanceState.value.copy(
                    duplicates = duplicates,
                    lastScanMessage = message
                )
                _messageEvent.emit(message)
            } catch (e: Exception) {
                val message = "Scan fehlgeschlagen: ${e.message ?: "unbekannter Fehler"}"
                _maintenanceState.value = _maintenanceState.value.copy(lastScanMessage = message)
                _messageEvent.emit(message)
            } finally {
                _maintenanceState.value = _maintenanceState.value.copy(isScanning = false)
            }
        }
    }

    fun importFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val result = storage.importFromUri(uri)
            _maintenanceState.value = _maintenanceState.value.copy(duplicates = storage.getDetectedDuplicates())
            if (result != null) {
                _messageEvent.emit("Hörbuch '${result.title}' erfolgreich importiert")
            } else {
                _messageEvent.emit("Fehler beim Importieren der Datei")
            }
        }
    }

    fun importFromFolderUri(uri: android.net.Uri) {
        viewModelScope.launch {
            storage.clearDetectedDuplicates()
            val count = storage.importFromFolderUri(uri)
            _maintenanceState.value = _maintenanceState.value.copy(duplicates = storage.getDetectedDuplicates())
            if (count > 0) {
                _messageEvent.emit("$count Hörbuch(er) erfolgreich aus Ordner importiert")
            } else if (_maintenanceState.value.duplicates.isNotEmpty()) {
                _messageEvent.emit("${_maintenanceState.value.duplicates.size} identische(s) Duplikat(e) gefunden")
            } else {
                _messageEvent.emit("Keine neuen Hörbücher im ausgewählten Ordner gefunden.")
            }
        }
    }

    fun deleteDuplicate(match: de.f_soft_studio.abookplayer.util.DuplicateMatch) {
        viewModelScope.launch {
            val success = storage.deleteDuplicateFromStorage(match)
            _maintenanceState.value = _maintenanceState.value.copy(duplicates = storage.getDetectedDuplicates())
            if (success) {
                _messageEvent.emit("Doppelgänger auf dem Speicher gelöscht.")
            } else {
                _messageEvent.emit("Fehler beim Löschen des Doppelgängers.")
            }
        }
    }

    fun cleanupLibrary() {
        viewModelScope.launch {
            _maintenanceState.value = _maintenanceState.value.copy(isCleaning = true)
            try {
                val result = repository.cleanupDuplicatesAndOrphans(context)
                _maintenanceState.value = _maintenanceState.value.copy(cleanupResult = result)

                val parts = mutableListOf<String>()
                if (result.duplicatesRemoved > 0) parts.add("${result.duplicatesRemoved} doppelte(r)")
                if (result.orphansRemoved > 0) parts.add("${result.orphansRemoved} verwaiste(r)")
                if (result.zeroDurationFixed > 0) parts.add("${result.zeroDurationFixed} 0-min-Hörbuch(er) repariert")
                val message = if (parts.isNotEmpty()) {
                    "Aufräumen beendet: ${parts.joinToString(", ")}."
                } else {
                    "Bibliothek ist bereits sauber. Keine fehlerhaften Einträge."
                }
                _messageEvent.emit(message)
            } finally {
                _maintenanceState.value = _maintenanceState.value.copy(isCleaning = false)
            }
        }
    }

    fun dismissDuplicate(match: de.f_soft_studio.abookplayer.util.DuplicateMatch) {
        _maintenanceState.value = _maintenanceState.value.copy(
            duplicates = _maintenanceState.value.duplicates.filterNot { it == match }
        )
    }

    fun importAudiobook(audiobook: Audiobook) {
        viewModelScope.launch {
            repository.saveAudiobook(audiobook)
        }
    }

    fun deleteAudiobook(id: Long) {
        viewModelScope.launch {
            repository.deleteAudiobook(id)
            _messageEvent.emit("Hörbuch gelöscht")
        }
    }
}

