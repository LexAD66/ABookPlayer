package de.f_soft_studio.abookplayer.ui

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.f_soft_studio.abookplayer.data.local.db.AbookDatabase
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.storage.AbookStorage
import de.f_soft_studio.abookplayer.ui.library.LibraryViewModel
import de.f_soft_studio.abookplayer.ui.library.SortOrder
import de.f_soft_studio.abookplayer.ui.library.StatusFilter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit-Test für LibraryViewModel (Suche, Sortierung, Flow-Filterung, Batch-Aktionen).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LibraryViewModelTest {

    private lateinit var database: AbookDatabase
    private lateinit var repository: AudiobookRepository
    private lateinit var storage: AbookStorage
    private lateinit var viewModel: LibraryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AbookDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = AudiobookRepository(
            audiobookDao = database.audiobookDao(),
            chapterDao = database.chapterDao(),
            bookmarkDao = database.bookmarkDao()
        )

        storage = mockk(relaxed = true)
        coEvery { storage.scanAndImport() } returns emptyList()
        viewModel = LibraryViewModel(repository, storage)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun testSearchQueryFiltering() = runBlocking {
        repository.saveAudiobook(
            Audiobook(title = "Der Alchemist", author = "Paulo Coelho", filePath = "/path1", coverUri = null, duration = 1000L, currentPosition = 0L, lastPlayed = 100L)
        )
        repository.saveAudiobook(
            Audiobook(title = "Die Verwandlung", author = "Franz Kafka", filePath = "/path2", coverUri = null, duration = 2000L, currentPosition = 0L, lastPlayed = 200L)
        )

        viewModel.onSearchQueryChanged("Alchemist")

        val filtered = viewModel.audiobooks.first { it.isNotEmpty() }
        assertEquals(1, filtered.size)
        assertEquals("Der Alchemist", filtered[0].title)
    }

    @Test
    fun testSearchQueryByNarrator() = runBlocking {
        repository.saveAudiobook(
            Audiobook(title = "Hörbuch 1", author = "Autor 1", narrator = "David Nathan", filePath = "/path1", coverUri = null, duration = 1000L, currentPosition = 0L, lastPlayed = 100L)
        )
        repository.saveAudiobook(
            Audiobook(title = "Hörbuch 2", author = "Autor 2", narrator = "Rufus Beck", filePath = "/path2", coverUri = null, duration = 2000L, currentPosition = 0L, lastPlayed = 200L)
        )

        viewModel.onSearchQueryChanged("Nathan")

        val filtered = viewModel.audiobooks.first { it.isNotEmpty() }
        assertEquals(1, filtered.size)
        assertEquals("David Nathan", filtered[0].narrator)
    }

    @Test
    fun testSortOrderByTitle() = runBlocking {
        repository.saveAudiobook(
            Audiobook(title = "B-Titel", author = "Autor 1", filePath = "/path1", coverUri = null, duration = 1000L, currentPosition = 0L, lastPlayed = 100L)
        )
        repository.saveAudiobook(
            Audiobook(title = "A-Titel", author = "Autor 2", filePath = "/path2", coverUri = null, duration = 2000L, currentPosition = 0L, lastPlayed = 200L)
        )

        viewModel.onSortOrderChanged(SortOrder.TITEL)

        val sorted = viewModel.audiobooks.first { it.size == 2 }
        assertEquals(2, sorted.size)
        assertEquals("A-Titel", sorted[0].title)
        assertEquals("B-Titel", sorted[1].title)
    }

    @Test
    fun testSortOrderByRemainingTime() = runBlocking {
        repository.saveAudiobook(
            Audiobook(title = "Langes Buch", author = "Autor 1", filePath = "/path1", coverUri = null, duration = 10000L, currentPosition = 0L, lastPlayed = 100L)
        )
        repository.saveAudiobook(
            Audiobook(title = "Fast fertig", author = "Autor 2", filePath = "/path2", coverUri = null, duration = 10000L, currentPosition = 9000L, lastPlayed = 200L)
        )

        viewModel.onSortOrderChanged(SortOrder.RESTLAUFZEIT)

        val sorted = viewModel.audiobooks.first { it.size == 2 }
        assertEquals(2, sorted.size)
        assertEquals("Fast fertig", sorted[0].title)
        assertEquals("Langes Buch", sorted[1].title)
    }

    @Test
    fun testFavoritesFilter() = runBlocking {
        val id1 = repository.saveAudiobook(
            Audiobook(title = "Buch 1", author = "Autor 1", filePath = "/path1", coverUri = null, duration = 1000L, currentPosition = 0L, lastPlayed = 100L)
        )
        repository.saveAudiobook(
            Audiobook(title = "Buch 2", author = "Autor 2", filePath = "/path2", coverUri = null, duration = 2000L, currentPosition = 0L, lastPlayed = 200L)
        )

        viewModel.toggleFavorite(id1)
        viewModel.onStatusFilterChanged(StatusFilter.FAVORITEN)

        val favs = viewModel.audiobooks.first { it.isNotEmpty() }
        assertEquals(1, favs.size)
        assertEquals("Buch 1", favs[0].title)
    }

    @Test
    fun testBatchDeleteAndBatchFavorite() = runBlocking {
        val id1 = repository.saveAudiobook(
            Audiobook(title = "Buch 1", author = "Autor 1", filePath = "/path1", coverUri = null, duration = 1000L, currentPosition = 0L, lastPlayed = 100L)
        )
        val id2 = repository.saveAudiobook(
            Audiobook(title = "Buch 2", author = "Autor 2", filePath = "/path2", coverUri = null, duration = 2000L, currentPosition = 0L, lastPlayed = 200L)
        )

        viewModel.toggleBookSelection(id1)
        viewModel.toggleBookSelection(id2)
        viewModel.toggleFavoriteForSelected()

        val favoriteIds = viewModel.favoriteBookIds.first { it.contains(id1) && it.contains(id2) }
        assertTrue(favoriteIds.contains(id1))
        assertTrue(favoriteIds.contains(id2))

        viewModel.toggleBookSelection(id1)
        viewModel.toggleBookSelection(id2)
        viewModel.deleteSelectedAudiobooks()

        val booksAfterDelete = viewModel.audiobooks.first { it.isEmpty() }
        assertTrue(booksAfterDelete.isEmpty())
    }


    @Test
    fun testSeriesStackGrouping() = runBlocking {
        repository.saveAudiobook(
            Audiobook(title = "Band 1", author = "Autor A", series = "Die drei ???", seriesOrder = 1, filePath = "/path1")
        )
        repository.saveAudiobook(
            Audiobook(title = "Band 2", author = "Autor A", series = "Die drei ???", seriesOrder = 2, filePath = "/path2")
        )
        repository.saveAudiobook(
            Audiobook(title = "Einzelbuch", author = "Autor B", series = null, filePath = "/path3")
        )

        viewModel.onSortOrderChanged(SortOrder.SERIEN)

        val items = viewModel.libraryItems.first { it.size == 2 }
        assertEquals(2, items.size)

        val seriesItem = items.first { it is de.f_soft_studio.abookplayer.ui.library.LibraryItem.Series } as de.f_soft_studio.abookplayer.ui.library.LibraryItem.Series
        assertEquals("Die drei ???", seriesItem.stack.seriesName)
        assertEquals(2, seriesItem.stack.books.size)

        viewModel.openSeries(seriesItem.stack)
        assertEquals("Die drei ???", viewModel.expandedSeries.value?.seriesName)

        viewModel.closeSeries()
        assertEquals(null, viewModel.expandedSeries.value)
    }

    @Test
    fun testTwoLevelSeriesHierarchyPerryRhodanAtlantis() = runBlocking {
        repository.saveAudiobook(
            Audiobook(
                title = "Das dunkle Reich",
                author = "K. H. Scheer",
                parentSeries = "Perry Rhodan",
                series = "Atlantis",
                seriesOrder = 1,
                filePath = "/path/pr1"
            )
        )
        repository.saveAudiobook(
            Audiobook(
                title = "Die Kristallwelt",
                author = "Clark Darlton",
                parentSeries = "Perry Rhodan",
                series = "Atlantis",
                seriesOrder = 2,
                filePath = "/path/pr2"
            )
        )

        viewModel.onSortOrderChanged(SortOrder.SERIEN)

        val items = viewModel.libraryItems.first { it.isNotEmpty() }
        assertEquals(1, items.size)

        val seriesItem = items.first() as de.f_soft_studio.abookplayer.ui.library.LibraryItem.Series
        assertEquals("Perry Rhodan - Atlantis", seriesItem.stack.seriesName)
        assertEquals("Perry Rhodan", seriesItem.stack.parentSeries)
        assertEquals(2, seriesItem.stack.books.size)
        assertEquals(1, seriesItem.stack.books[0].seriesOrder)
        assertEquals(2, seriesItem.stack.books[1].seriesOrder)
    }

    @Test
    fun testAddedAtDefaultsToNowAndPersistsExplicitValue() = runBlocking {
        val idDefault = repository.saveAudiobook(
            Audiobook(title = "Ohne Datum", author = "A", filePath = "/p1")
        )
        val idExplicit = repository.saveAudiobook(
            Audiobook(title = "Mit Datum", author = "A", filePath = "/p2", addedAt = 1_700_000_000_000L)
        )

        val all = repository.getAllAudiobooks().first { it.size == 2 }
        val byDefault = all.first { it.id == idDefault }
        val byExplicit = all.first { it.id == idExplicit }

        assertTrue("Default-addedAt sollte gesetzt sein", byDefault.addedAt > 0L)
        assertEquals(1_700_000_000_000L, byExplicit.addedAt)
    }

    @Test
    fun testSortByAddedAtUsesAddedAtNotId() = runBlocking {
        // Zuerst gespeichert (kleine id), aber neueres addedAt:
        repository.saveAudiobook(
            Audiobook(title = "Zuerst gespeichert", author = "A", filePath = "/p1", addedAt = 2_000L)
        )
        // Danach gespeichert (größere id), aber älteres addedAt:
        repository.saveAudiobook(
            Audiobook(title = "Danach gespeichert", author = "A", filePath = "/p2", addedAt = 1_000L)
        )

        viewModel.onSortOrderChanged(SortOrder.HINZUGEFUEGT_AM)

        val sorted = viewModel.audiobooks.first { it.size == 2 }
        assertEquals("Zuerst gespeichert", sorted[0].title) // größeres addedAt zuerst
        assertEquals("Danach gespeichert", sorted[1].title)
    }

    @Test
    fun testScanUpdatesMaintenanceState() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val gatedStorage: AbookStorage = mockk(relaxed = true)
        coEvery { gatedStorage.scanAndImport() } coAnswers {
            gate.await()
            emptyList()
        }
        val vm = LibraryViewModel(repository, gatedStorage)

        assertEquals(false, vm.maintenanceState.value.isScanning)

        vm.scanAudiobooks()
        assertTrue("Scan sollte als laufend markiert sein", vm.maintenanceState.value.isScanning)

        gate.complete(Unit)
        val done = vm.maintenanceState.first { !it.isScanning }
        assertEquals(false, done.isScanning)
        assertTrue("lastScanMessage sollte gesetzt sein", !done.lastScanMessage.isNullOrBlank())
    }

    @Test
    fun testDetectedDuplicatesStillExposedFromMaintenanceState() = runBlocking {
        // Nach einem Scan ohne Duplikate ist die Liste leer und der Flow lieferbar.
        viewModel.scanAudiobooks()
        val dups = viewModel.detectedDuplicates.first()
        assertTrue(dups.isEmpty())
        assertEquals(dups, viewModel.maintenanceState.value.duplicates)
    }

    @Test
    fun testDetectedDuplicatesPropagatedFromStorageAfterScan() = runBlocking {
        // Frischer Mock, damit getDetectedDuplicates() eine echte Liste liefert.
        val dupStorage: AbookStorage = mockk(relaxed = true)
        val match = de.f_soft_studio.abookplayer.util.DuplicateMatch(
            existingAudiobook = Audiobook(title = "Der Alchemist", author = "Paulo Coelho", filePath = "/lib/alchemist"),
            candidateTitle = "Der Alchemist",
            candidatePath = "/scan/alchemist",
            comparisonType = de.f_soft_studio.abookplayer.util.ComparisonType.IDENTICAL_DUPLICATE,
            durationDifferenceMs = 0L
        )
        val expected = listOf(match)
        coEvery { dupStorage.scanAndImport() } returns emptyList()
        coEvery { dupStorage.getDetectedDuplicates() } returns expected

        val vm = LibraryViewModel(repository, dupStorage)
        vm.scanAudiobooks()

        assertEquals(expected, vm.detectedDuplicates.first { it.isNotEmpty() })
        assertEquals(expected, vm.maintenanceState.value.duplicates)
    }

    @Test
    fun testInitDoesNotTriggerAutomaticScan() = runBlocking {
        // frisches ViewModel mit eigenem Mock, um Aufrufe isoliert zu prüfen
        val freshStorage: AbookStorage = mockk(relaxed = true)
        LibraryViewModel(repository, freshStorage)

        coVerify(exactly = 0) { freshStorage.scanAndImport() }
    }
}

