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
 * Repräsentiert eine gebündelte Buchreihe in der Bibliothek.
 */
data class SeriesStack(
    val seriesName: String,
    val author: String,
    val books: List<Audiobook>,
    val totalDuration: Long,
    val totalProgress: Float,
    val coverUri: String?
)

/**
 * Ein Bibliothekselement: Weder ein Einzelbuch ODER ein Serien-Stapel.
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
    private val storage: AbookStorage
) : ViewModel() {

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.ZULETZT_GEHOERT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _statusFilter = MutableStateFlow(StatusFilter.ALLE)
    val statusFilter: StateFlow<StatusFilter> = _statusFilter.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _selectedBookIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedBookIds: StateFlow<Set<Long>> = _selectedBookIds.asStateFlow()

    private val _favoriteBookIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteBookIds: StateFlow<Set<Long>> = _favoriteBookIds.asStateFlow()

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
        val current = _favoriteBookIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _favoriteBookIds.value = current
    }

    fun toggleFavoriteForSelected() {
        val selected = _selectedBookIds.value
        if (selected.isEmpty()) return
        val currentFavs = _favoriteBookIds.value.toMutableSet()
        val allSelectedAreFavs = selected.all { currentFavs.contains(it) }
        if (allSelectedAreFavs) {
            currentFavs.removeAll(selected)
        } else {
            currentFavs.addAll(selected)
        }
        _favoriteBookIds.value = currentFavs
        _selectedBookIds.value = emptySet()
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
                    repository.saveAudiobook(it.copy(currentPosition = it.duration))
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
        _statusFilter,
        _favoriteBookIds
    ) { books, query, sort, status, favs ->
        val searchFiltered = if (query.isBlank()) {
            books
        } else {
            books.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true) ||
                (it.series?.contains(query, ignoreCase = true) == true) ||
                (it.narrator?.contains(query, ignoreCase = true) == true)
            }
        }

        val statusFiltered = when (status) {
            StatusFilter.ALLE -> searchFiltered
            StatusFilter.ANGEFANGEN -> searchFiltered.filter { it.currentPosition > 0L && (it.duration <= 0L || it.currentPosition < it.duration - 5000L) }
            StatusFilter.BEENDET -> searchFiltered.filter { it.duration > 0L && it.currentPosition >= it.duration - 5000L }
            StatusFilter.UNGESPIELT -> searchFiltered.filter { it.currentPosition == 0L }
            StatusFilter.FAVORITEN -> searchFiltered.filter { favs.contains(it.id) }
        }

        when (sort) {
            SortOrder.ZULETZT_GEHOERT -> statusFiltered.sortedByDescending { it.lastPlayed }
            SortOrder.TITEL -> statusFiltered.sortedBy { it.title.lowercase() }
            SortOrder.AUTOR -> statusFiltered.sortedBy { it.author.lowercase() }
            SortOrder.SERIEN -> statusFiltered.sortedWith(
                compareBy<Audiobook> { it.series?.lowercase() ?: "zzzz" }
                    .thenBy { it.seriesOrder ?: 999 }
                    .thenBy { it.title.lowercase() }
            )
            SortOrder.RESTLAUFZEIT -> statusFiltered.sortedBy { maxOf(0L, it.duration - it.currentPosition) }
            SortOrder.HINZUGEFUEGT_AM -> statusFiltered.sortedByDescending { it.id }
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
            val (seriesBooks, standaloneBooks) = books.partition { !it.series.isNullOrBlank() }
            val seriesGroups = seriesBooks.groupBy { it.series!!.trim() }

            val items = mutableListOf<LibraryItem>()

            seriesGroups.forEach { (seriesName, group) ->
                val sortedGroup = group.sortedBy { it.seriesOrder ?: 999 }
                if (sortedGroup.size >= 2) {
                    val author = sortedGroup.firstOrNull { it.author.isNotBlank() }?.author ?: ""
                    val totalDuration = sortedGroup.sumOf { it.duration }
                    val totalPosition = sortedGroup.sumOf { it.currentPosition }
                    val totalProgress = if (totalDuration > 0) (totalPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f
                    val cover = sortedGroup.firstOrNull { !it.coverUri.isNullOrBlank() }?.coverUri
                    items.add(
                        LibraryItem.Series(
                            SeriesStack(
                                seriesName = seriesName,
                                author = author,
                                books = sortedGroup,
                                totalDuration = totalDuration,
                                totalProgress = totalProgress,
                                coverUri = cover
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

    init {
        scanAudiobooks()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChanged(order: SortOrder) {
        _sortOrder.value = order
    }

    fun onStatusFilterChanged(filter: StatusFilter) {
        _statusFilter.value = filter
    }

    fun scanAudiobooks() {
        viewModelScope.launch {
            val imported = storage.scanAndImport()
            if (imported.isNotEmpty()) {
                _messageEvent.emit("${imported.size} neue(s) Hörbuch(er) importiert")
            } else {
                _messageEvent.emit("Scan beendet. Keine neuen .abook-Dateien gefunden.")
            }
        }
    }

    fun importFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val result = storage.importFromUri(uri)
            if (result != null) {
                _messageEvent.emit("Hörbuch '${result.title}' erfolgreich importiert")
            } else {
                _messageEvent.emit("Fehler beim Importieren der Datei")
            }
        }
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
