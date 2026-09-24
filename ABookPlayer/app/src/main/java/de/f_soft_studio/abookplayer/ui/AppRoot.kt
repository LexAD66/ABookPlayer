package de.f_soft_studio.abookplayer.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.f_soft_studio.abookplayer.ui.info.EbookInfoScreen
import de.f_soft_studio.abookplayer.ui.library.LibraryManagementScreen
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
 * AppRoot: Navigations-Hauptansicht der ABook Player Anwendung mit flüssigen Übergangsanimationen.
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

    val libraryViewModel = remember { LibraryViewModel(repository, storage, context) }
    val playerViewModel = remember { PlayerViewModel(playbackController) }
    val statisticsViewModel = remember {
        StatisticsViewModel(GetListeningStatisticsUseCase(repository))
    }
    val settingsViewModel = remember { de.f_soft_studio.abookplayer.ui.settings.SettingsViewModel(context, storage) }
    val charactersViewModel = remember { de.f_soft_studio.abookplayer.ui.characters.CharactersViewModel(repository) }
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

    // Wiedergabe-Fehler (z. B. fehlende/verschobene Audiodateien) sichtbar machen.
    LaunchedEffect(Unit) {
        playbackController.playbackError.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val bookmarksState = currentAudiobook?.let { book ->
        repository.getBookmarksForAudiobook(book.id).collectAsState(initial = emptyList())
    }
    val bookmarks = bookmarksState?.value ?: emptyList()

    ABookTheme(themeMode = appThemeMode) {
        NavHost(
            navController = navController,
            startDestination = "library",
            enterTransition = {
                fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(280)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(220)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(220)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(280)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(220)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(220)
                )
            }
        ) {
        composable(
            route = "library",
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
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
                    if (currentAudiobook?.id == book.id) {
                        navController.navigate("player")
                    } else {
                        scope.launch {
                            val chapterList = repository.getChaptersForAudiobook(book.id).first()
                            playerViewModel.loadAudiobook(book, chapterList)
                        }
                        navController.navigate("player")
                    }
                },
                onOpenDetails = { book ->
                    selectedBookForDetails = book
                    navController.navigate("details")
                },
                onImportRequested = {
                    libraryViewModel.scanAudiobooks()
                },
                onOpenLibraryManagement = {
                    navController.navigate("library_management")
                },
                onOpenStatistics = {
                    navController.navigate("statistics")
                },
                onOpenSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("library_management") {
            LibraryManagementScreen(
                viewModel = libraryViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("settings") {
            de.f_soft_studio.abookplayer.ui.settings.SettingsScreen(
                viewModel = settingsViewModel,
                repository = repository,
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
            var isSearchingOnline by remember { mutableStateOf(false) }

            var showEditDialog by remember { mutableStateOf(false) }

            DetailsScreen(
                audiobook = book,
                chapters = detailsChapters,
                exportState = exportState,
                isSearchingOnline = isSearchingOnline,
                onPlayClick = {
                    book?.let { b ->
                        if (currentAudiobook?.id == b.id) {
                            if (!isPlaying) {
                                playbackController.play()
                            }
                            navController.navigate("player")
                        } else {
                            scope.launch {
                                val chapterList = repository.getChaptersForAudiobook(b.id).first()
                                playerViewModel.loadAudiobook(b, chapterList)
                            }
                            navController.navigate("player")
                        }
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
                    if (book != null && !isSearchingOnline) {
                        isSearchingOnline = true
                        scope.launch {
                            try {
                                val scraper = de.f_soft_studio.abookplayer.storage.OnlineCoverScraper(context)
                                val result = scraper.searchCoverAndMetadata(book.title, book.author)
                                if (result != null && (!result.coverPath.isNullOrBlank() || !result.description.isNullOrBlank() || !result.narrator.isNullOrBlank() || !result.series.isNullOrBlank())) {
                                    val updatedBook = book.copy(
                                        coverUri = result.coverPath ?: book.coverUri,
                                        description = result.description ?: book.description,
                                        narrator = if (book.narrator.isNullOrBlank()) result.narrator ?: book.narrator else book.narrator,
                                        series = if (book.series.isNullOrBlank()) result.series ?: book.series else book.series,
                                        seriesOrder = if (book.seriesOrder == null) result.seriesOrder ?: book.seriesOrder else book.seriesOrder
                                    )
                                    repository.saveAudiobook(updatedBook)
                                    selectedBookForDetails = updatedBook
                                    android.widget.Toast.makeText(context, "Metadaten via ${result.providerName} gefunden!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Keine Online-Daten gefunden.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Fehler bei der Online-Suche: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            } finally {
                                isSearchingOnline = false
                            }
                        }
                    }
                },
                onEditClick = { showEditDialog = true },
                onOpenCharacters = {
                    if (book != null) {
                        navController.navigate("characters/${book.id}")
                    }
                },
                onOpenInfo = { b ->
                    selectedBookForDetails = b
                    navController.navigate("info")
                },
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
                    onConfirm = { title, author, narrator, parentSeries, series, seriesOrder ->
                        showEditDialog = false
                        scope.launch {
                            val updatedBook = book.copy(
                                 title = title,
                                 author = author,
                                 narrator = narrator,
                                 parentSeries = parentSeries,
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

        composable("info") {
            val book = selectedBookForDetails
            EbookInfoScreen(
                audiobook = book,
                onBackClick = { navController.popBackStack() },
                onSearchOnline = { b ->
                    scope.launch {
                        try {
                            val scraper = de.f_soft_studio.abookplayer.storage.OnlineCoverScraper(context)
                            val result = scraper.searchCoverAndMetadata(b.title, b.author)
                            if (result != null && (!result.coverPath.isNullOrBlank() || !result.description.isNullOrBlank() || !result.narrator.isNullOrBlank() || !result.series.isNullOrBlank())) {
                                val updatedBook = b.copy(
                                    coverUri = result.coverPath ?: b.coverUri,
                                    description = result.description ?: b.description,
                                    narrator = if (b.narrator.isNullOrBlank()) result.narrator ?: b.narrator else b.narrator,
                                    series = if (b.series.isNullOrBlank()) result.series ?: b.series else b.series,
                                    seriesOrder = if (b.seriesOrder == null) result.seriesOrder ?: b.seriesOrder else b.seriesOrder
                                )
                                repository.saveAudiobook(updatedBook)
                                selectedBookForDetails = updatedBook
                                android.widget.Toast.makeText(context, "Metadaten via ${result.providerName} gefunden!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Keine Online-Daten gefunden.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Fehler bei der Online-Suche: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onEditMetadata = { b ->
                    selectedBookForDetails = b
                    navController.navigate("details")
                }
            )
        }

        composable(
            route = "player",
            enterTransition = {
                fadeIn(animationSpec = tween(320)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(320)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(260))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(260))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(260)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(260)
                )
            }
        ) {
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
            var showEditChaptersDialog by remember { mutableStateOf(false) }

            ChaptersScreen(
                chapters = chapters,
                currentChapterId = currentChapter?.id,
                onChapterSelected = { ch ->
                    playerViewModel.seekTo(ch.startTime)
                    navController.popBackStack()
                },
                onEditChaptersRequested = { showEditChaptersDialog = true },
                onBackClick = { navController.popBackStack() }
            )

            if (showEditChaptersDialog && currentAudiobook != null) {
                de.f_soft_studio.abookplayer.ui.chapters.EditChaptersDialog(
                    audiobookId = currentAudiobook!!.id,
                    initialChapters = chapters,
                    onDismiss = { showEditChaptersDialog = false },
                    onConfirm = { updatedChapters ->
                        showEditChaptersDialog = false
                        scope.launch {
                            repository.saveChapters(updatedChapters)
                            currentAudiobook?.let { book ->
                                playbackController.loadAudiobook(book, updatedChapters, autoPlay = false)
                            }
                        }
                    }
                )
            }
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

        composable("characters/{audiobookId}") { backStackEntry ->
            val bookIdStr = backStackEntry.arguments?.getString("audiobookId")
            val bookId = bookIdStr?.toLongOrNull() ?: 0L
            de.f_soft_studio.abookplayer.ui.characters.CharactersScreen(
                viewModel = charactersViewModel,
                audiobookId = bookId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }


    if (showSleepTimerDialog) {
        val isShakeEnabled by playbackController.sleepTimerController.isShakeToResetEnabled.collectAsState()
        val isFadeEnabled by playbackController.sleepTimerController.isFadeOutEnabled.collectAsState()
        SleepTimerDialog(
            isActive = isTimerActive,
            remainingTimeMs = remainingTimerMs,
            isShakeToResetEnabled = isShakeEnabled,
            isFadeOutEnabled = isFadeEnabled,
            onToggleShakeToReset = { playbackController.sleepTimerController.setShakeToResetEnabled(it) },
            onToggleFadeOut = { playbackController.sleepTimerController.setFadeOutEnabled(it) },
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
