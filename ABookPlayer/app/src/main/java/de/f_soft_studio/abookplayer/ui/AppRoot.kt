package de.f_soft_studio.abookplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Bookmark
import de.f_soft_studio.abookplayer.player.controller.PlaybackController
import de.f_soft_studio.abookplayer.storage.AbookStorage
import de.f_soft_studio.abookplayer.ui.bookmarks.BookmarksScreen
import de.f_soft_studio.abookplayer.ui.chapters.ChaptersScreen
import de.f_soft_studio.abookplayer.ui.library.LibraryScreen
import de.f_soft_studio.abookplayer.ui.library.LibraryViewModel
import de.f_soft_studio.abookplayer.ui.player.PlayerScreen
import de.f_soft_studio.abookplayer.ui.player.PlayerViewModel
import de.f_soft_studio.abookplayer.ui.sleep.SleepTimerDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.ui.details.DetailsScreen

import de.f_soft_studio.abookplayer.domain.usecase.GetListeningStatisticsUseCase
import de.f_soft_studio.abookplayer.ui.statistics.StatisticsScreen
import de.f_soft_studio.abookplayer.ui.statistics.StatisticsViewModel

import androidx.compose.ui.platform.LocalContext
import de.f_soft_studio.abookplayer.domain.model.ExportState

import de.f_soft_studio.abookplayer.ui.theme.ABookTheme

/**
 * AppRoot: Navigations-Hauptansicht der ABook Player Anwendung.
 */
@Composable
fun AppRoot(
    repository: AudiobookRepository,
    storage: AbookStorage,
    playbackController: PlaybackController
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val libraryViewModel = remember { LibraryViewModel(repository, storage) }
    val playerViewModel = remember { PlayerViewModel(playbackController) }
    val statisticsViewModel = remember {
        StatisticsViewModel(GetListeningStatisticsUseCase(repository))
    }
    val settingsViewModel = remember { de.f_soft_studio.abookplayer.ui.settings.SettingsViewModel(context) }
    val appThemeMode by settingsViewModel.appThemeMode.collectAsState()

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var selectedBookForDetails by remember { mutableStateOf<Audiobook?>(null) }
    val isTimerActive by playbackController.sleepTimerController.isActive.collectAsState()
    val remainingTimerMs by playbackController.sleepTimerController.remainingTimeMs.collectAsState()

    val currentAudiobook by playbackController.currentAudiobook.collectAsState()
    val isPlaying by playbackController.isPlaying.collectAsState()
    val currentPosition by playbackController.currentPosition.collectAsState()
    val duration by playbackController.duration.collectAsState()
    val chapters by playbackController.chapters.collectAsState()
    val currentChapter by playbackController.currentChapter.collectAsState()

    val bookmarksState = currentAudiobook?.let { book ->
        repository.getBookmarksForAudiobook(book.id).collectAsState(initial = emptyList())
    }
    val bookmarks = bookmarksState?.value ?: emptyList()

    ABookTheme(themeMode = appThemeMode) {
        NavHost(
            navController = navController,
            startDestination = "library"
        ) {
        composable("library") {
            LibraryScreen(
                viewModel = libraryViewModel,
                currentAudiobook = currentAudiobook,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                isSleepTimerActive = isTimerActive,
                remainingTimerMs = remainingTimerMs,
                onTogglePlayPause = { playbackController.togglePlayPause() },
                onSkip10sForward = { playbackController.skip10SecondsForward() },
                onOpenPlayer = { navController.navigate("player") },
                onAudiobookSelected = { book ->
                    scope.launch {
                        val chapterList = repository.getChaptersForAudiobook(book.id).first()
                        playerViewModel.loadAudiobook(book, chapterList)
                    }
                    navController.navigate("player")
                },
                onOpenDetails = { book ->
                    selectedBookForDetails = book
                    navController.navigate("details")
                },
                onImportRequested = {
                    libraryViewModel.scanAudiobooks()
                },
                onOpenStatistics = {
                    navController.navigate("statistics")
                },
                onOpenSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            de.f_soft_studio.abookplayer.ui.settings.SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("statistics") {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("details") {
            val book = selectedBookForDetails
            val detailsChaptersState = book?.let { b ->
                repository.getChaptersForAudiobook(b.id).collectAsState(initial = emptyList())
            }
            val detailsChapters = detailsChaptersState?.value ?: emptyList()
            var exportState by remember { mutableStateOf<ExportState>(ExportState.Idle) }

            var showEditDialog by remember { mutableStateOf(false) }

            DetailsScreen(
                audiobook = book,
                chapters = detailsChapters,
                exportState = exportState,
                onPlayClick = {
                    book?.let { b ->
                        scope.launch {
                            val chapterList = repository.getChaptersForAudiobook(b.id).first()
                            playerViewModel.loadAudiobook(b, chapterList)
                        }
                        navController.navigate("player")
                    }
                },
                onExportToUri = { uri ->
                    if (book != null) {
                        scope.launch {
                            try {
                                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    storage.exportToAbook(book, detailsChapters, outputStream).collect { state ->
                                        exportState = state
                                    }
                                }
                            } catch (e: Exception) {
                                exportState = ExportState.Error("Export-Fehler: ${e.message}")
                            }
                        }
                    }
                },
                onSearchCoverOnline = {
                    if (book != null) {
                        scope.launch {
                            val scraper = de.f_soft_studio.abookplayer.storage.OpenLibraryScraper(context)
                            val result = scraper.searchMetadataAndCover(book.title, book.author)
                            if (result != null && (!result.coverPath.isNullOrBlank() || !result.description.isNullOrBlank())) {
                                repository.updateCoverAndDescription(book.id, result.coverPath, result.description)
                                val updated = repository.getAudiobookById(book.id)
                                if (updated != null) {
                                    selectedBookForDetails = updated
                                }
                            }
                        }
                    }
                },
                onEditClick = { showEditDialog = true },
                onUpdateMetadata = { updatedBook ->
                    scope.launch {
                        repository.saveAudiobook(updatedBook)
                        selectedBookForDetails = updatedBook
                    }
                },
                onBackClick = { navController.popBackStack() }
            )

            if (showEditDialog && book != null) {
                de.f_soft_studio.abookplayer.ui.details.EditAudiobookDialog(
                    audiobook = book,
                    onDismiss = { showEditDialog = false },
                    onConfirm = { title, author, narrator, series, seriesOrder ->
                        showEditDialog = false
                        scope.launch {
                            val updatedBook = book.copy(
                                title = title,
                                author = author,
                                narrator = narrator,
                                series = series,
                                seriesOrder = seriesOrder
                            )
                            repository.saveAudiobook(updatedBook)
                            selectedBookForDetails = updatedBook
                        }
                    }
                )
            }
        }

        composable("player") {
            PlayerScreen(
                viewModel = playerViewModel,
                onBackClick = { navController.popBackStack() },
                onOpenChapters = { navController.navigate("chapters") },
                onOpenBookmarks = { navController.navigate("bookmarks") },
                onOpenSleepTimer = { showSleepTimerDialog = true },
                onOpenCarMode = { navController.navigate("car_mode") }
            )
        }

        composable("car_mode") {
            de.f_soft_studio.abookplayer.ui.car.CarModeScreen(
                viewModel = playerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("chapters") {
            ChaptersScreen(
                chapters = chapters,
                currentChapterId = currentChapter?.id,
                onChapterSelected = { ch ->
                    playerViewModel.seekTo(ch.startTime)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("bookmarks") {
            BookmarksScreen(
                bookmarks = bookmarks,
                onAddBookmark = { note ->
                    currentAudiobook?.let { book ->
                        scope.launch {
                            repository.addBookmark(
                                Bookmark(
                                    audiobookId = book.id,
                                    position = currentPosition,
                                    note = note
                                )
                            )
                        }
                    }
                },
                onUpdateBookmark = { bm ->
                    scope.launch {
                        repository.updateBookmark(bm)
                    }
                },
                onBookmarkSelected = { bm ->
                    playerViewModel.seekTo(bm.position)
                    navController.popBackStack()
                },
                onDeleteBookmark = { bm ->
                    scope.launch {
                        repository.deleteBookmark(bm.id)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            isActive = isTimerActive,
            remainingTimeMs = remainingTimerMs,
            onSelectMinutes = { mins ->
                playbackController.sleepTimerController.startTimerMinutes(mins)
                currentAudiobook?.let { book ->
                    scope.launch {
                        repository.addBookmark(
                            Bookmark(
                                audiobookId = book.id,
                                position = currentPosition,
                                note = "Auto-Lesezeichen: Sleep-Timer ($mins Min)"
                            )
                        )
                    }
                }
            },
            onCancelTimer = {
                playbackController.sleepTimerController.cancelTimer()
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}
}
