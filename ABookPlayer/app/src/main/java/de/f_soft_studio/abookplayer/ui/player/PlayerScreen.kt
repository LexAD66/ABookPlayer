package de.f_soft_studio.abookplayer.ui.player

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import de.f_soft_studio.abookplayer.ui.common.AmbientCoverGlow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.f_soft_studio.abookplayer.util.CoverHelper
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

import de.f_soft_studio.abookplayer.ui.theme.DarkStageColorScheme

/**
 * PlayerScreen: Dunkle Stage-Wiedergabeoberfläche im Cinematic Look.
 * Unterstüzt:
 * 1. Unscharfen Cover-Art Blur-Hintergrund.
 * 2. Adaptives Layout für Hoch- und Querformat (Landscape Mode).
 * 3. Gestensteuerung auf dem Cover mit visueller Overlay-Rückmeldung (-10s / +10s / Play/Pause).
 * 4. Voice Boost (LoudnessEnhancer) Umschaltung.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    onOpenChapters: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenCarMode: () -> Unit = {}
) {
    val audiobook by viewModel.currentAudiobook.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val chaptersList by viewModel.chapters.collectAsState()
    val currentChapter by viewModel.currentChapter.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val isVolumeBoostEnabled by viewModel.isVolumeBoostEnabled.collectAsState()
    val isSkipSilenceEnabled by viewModel.isSkipSilenceEnabled.collectAsState()
    val audioPreset by viewModel.audioPreset.collectAsState()
    val loudnessGainMb by viewModel.loudnessGainMb.collectAsState()

    var showSpeedMenu by remember { mutableStateOf(false) }
    var showCustomSpeedDialog by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showPlayerOverflowMenu by remember { mutableStateOf(false) }
    var currentAudioProfile by remember { mutableStateOf(AudioProfile.SPRACHKLARHEIT) }
    val speedOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    MaterialTheme(colorScheme = DarkStageColorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Blur Cover or Dark Ambient Gradient
            val coverUri = audiobook?.coverUri
            val coverModel = remember(audiobook?.id, coverUri, audiobook?.filePath) {
                CoverHelper.resolveCoverModel(coverUri, audiobook?.filePath)
            }
            if (coverModel != null) {
                AsyncImage(
                    model = coverModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp)
                )
                // Dark Overlay for Contrast & Readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.65f),
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.surface,
                                    Color.Black
                                )
                            )
                        )
                )
            }

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Zurück",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            // Equalizer & Volume Boost Button
                            IconButton(onClick = { showEqualizerDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Sound-Equalizer & Klangprofil",
                                    tint = if (isVolumeBoostEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f)
                                )
                            }
                            // Speed Control Button & Dropdown Menu
                            Box {
                                TextButton(onClick = { showSpeedMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Geschwindigkeit",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.2fx", playbackSpeed),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false }
                                ) {
                                    speedOptions.forEach { speed ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${speed}x",
                                                    fontWeight = if (speed == playbackSpeed) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (speed == playbackSpeed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                viewModel.setSpeed(speed)
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Stufenlos...",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showSpeedMenu = false
                                            showCustomSpeedDialog = true
                                        }
                                    )
                                }
                            }
                            IconButton(onClick = { showChaptersSheet = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Kapitel",
                                    tint = Color.White
                                )
                            }
                            // M3 Overflow-Menü für sekundäre Aktionen
                            Box {
                                IconButton(onClick = { showPlayerOverflowMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Weitere Optionen",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = showPlayerOverflowMenu,
                                    onDismissRequest = { showPlayerOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Lesezeichen") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPlayerOverflowMenu = false
                                            onOpenBookmarks()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sleep-Timer") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPlayerOverflowMenu = false
                                            onOpenSleepTimer()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Auto-Modus") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPlayerOverflowMenu = false
                                            onOpenCarMode()
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { innerPadding ->
                if (isLandscape) {
                    // Landscape 2-Column Layout
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Side: Interactive Cover Art
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            CoverArtWithGestures(
                                coverUri = coverUri,
                                filePath = audiobook?.filePath,
                                title = audiobook?.title,
                                isPlaying = isPlaying,
                                onDoubleTap = { viewModel.togglePlayPause() },
                                onSwipeLeft = { viewModel.skip10sForward() },
                                onSwipeRight = { viewModel.skip10sBackward() },
                                modifier = Modifier
                                    .fillMaxHeight(0.85f)
                                    .aspectRatio(1f)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right Side: Details, Seekbar & Playback Controls
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Title & Author & Chapter Info
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = audiobook?.title ?: "Kein Hörbuch ausgewählt",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!audiobook?.author.isNullOrBlank()) {
                                    Text(
                                        text = audiobook?.author.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                val currentCh = currentChapter
                                if (currentCh != null) {
                                    val currentChIndex = chaptersList.indexOf(currentCh)
                                    val chStartTime = currentCh.startTime
                                    val chEndTime = if (currentChIndex >= 0 && currentChIndex < chaptersList.size - 1) {
                                        chaptersList[currentChIndex + 1].startTime
                                    } else {
                                        duration
                                    }
                                    val chDuration = (chEndTime - chStartTime).coerceAtLeast(0L)
                                    val chPosition = (currentPosition - chStartTime).coerceIn(0L, chDuration)

                                    Text(
                                        text = "Kapitel: ${currentCh.title} (${formatTimeMs(chPosition)} / ${formatTimeMs(chDuration)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            // Slider & Progress
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val maxRange = if (duration > 0) duration.toFloat() else 1f
                                val sliderPos = currentPosition.toFloat().coerceIn(0f, maxRange)
                                val remainingTimeMs = (duration - currentPosition).coerceAtLeast(0L)
                                val progressPercent = if (duration > 0) ((currentPosition * 100) / duration) else 0L

                                Slider(
                                    value = sliderPos,
                                    onValueChange = { viewModel.seekTo(it.toLong()) },
                                    valueRange = 0f..maxRange,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${formatTimeMs(currentPosition)} / ${formatTimeMs(duration)} ($progressPercent%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "-${formatTimeMs(remainingTimeMs)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Controls Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.skipPreviousChapter() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Vorheriges Kapitel",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.skip10sBackward() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clearAndSetSemantics { contentDescription = "10 Sekunden zurückspulen" }
                                ) {
                                    Text(
                                        text = "-10s",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.skip10sForward() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clearAndSetSemantics { contentDescription = "10 Sekunden vorspulen" }
                                ) {
                                    Text(
                                        text = "+10s",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.skipNextChapter() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Nächstes Kapitel",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Portrait 1-Column Layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Interactive Cover Art
                        CoverArtWithGestures(
                            coverUri = coverUri,
                            filePath = audiobook?.filePath,
                            title = audiobook?.title,
                            isPlaying = isPlaying,
                            onDoubleTap = { viewModel.togglePlayPause() },
                            onSwipeLeft = { viewModel.skip10sForward() },
                            onSwipeRight = { viewModel.skip10sBackward() },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title, Author & Current Chapter Info
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = audiobook?.title ?: "Kein Hörbuch ausgewählt",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!audiobook?.author.isNullOrBlank()) {
                                Text(
                                    text = audiobook?.author.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                            val currentCh = currentChapter
                            if (currentCh != null) {
                                val currentChIndex = chaptersList.indexOf(currentCh)
                                val chStartTime = currentCh.startTime
                                val chEndTime = if (currentChIndex >= 0 && currentChIndex < chaptersList.size - 1) {
                                    chaptersList[currentChIndex + 1].startTime
                                } else {
                                    duration
                                }
                                val chDuration = (chEndTime - chStartTime).coerceAtLeast(0L)
                                val chPosition = (currentPosition - chStartTime).coerceIn(0L, chDuration)

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Kapitel: ${currentCh.title} (${formatTimeMs(chPosition)} / ${formatTimeMs(chDuration)})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Slider & Total Time Display
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val maxRange = if (duration > 0) duration.toFloat() else 1f
                            val sliderPos = currentPosition.toFloat().coerceIn(0f, maxRange)
                            val remainingTimeMs = (duration - currentPosition).coerceAtLeast(0L)
                            val progressPercent = if (duration > 0) ((currentPosition * 100) / duration) else 0L

                            Slider(
                                value = sliderPos,
                                onValueChange = { viewModel.seekTo(it.toLong()) },
                                valueRange = 0f..maxRange,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Gesamtforschr.: ${formatTimeMs(currentPosition)} / ${formatTimeMs(duration)} ($progressPercent%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "-${formatTimeMs(remainingTimeMs)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Expanded Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.skipPreviousChapter() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Vorheriges Kapitel",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.skip10sBackward() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clearAndSetSemantics { contentDescription = "10 Sekunden zurückspulen" }
                            ) {
                                Text(
                                    text = "-10s",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.skip30sForward() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clearAndSetSemantics { contentDescription = "30 Sekunden vorspulen" }
                            ) {
                                Text(
                                    text = "+30s",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { viewModel.skipNextChapter() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Nächstes Kapitel",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showChaptersSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showChaptersSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kapitelübersicht",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = {
                            showChaptersSheet = false
                            onOpenChapters()
                        }) {
                            Text("Vollbild", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(chaptersList) { index, ch ->
                            val isCurrent = ch.id == currentChapter?.id
                            Surface(
                                onClick = {
                                    viewModel.seekTo(ch.startTime)
                                    showChaptersSheet = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${index + 1}. ${ch.title}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatTimeMs(ch.startTime),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isCurrent) {
                                        AudioWaveformVisualizer(isPlaying = isPlaying)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showCustomSpeedDialog) {
            var sliderSpeed by remember { mutableFloatStateOf(playbackSpeed) }
            AlertDialog(
                onDismissRequest = { showCustomSpeedDialog = false },
                title = { Text("Geschwindigkeit anpassen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = String.format(Locale.getDefault(), "Wiedergabe: %.2fx", sliderSpeed),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Slider(
                            value = sliderSpeed,
                            onValueChange = { sliderSpeed = (Math.round(it * 20) / 20f) },
                            valueRange = 0.5f..3.0f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.setSpeed(sliderSpeed)
                            showCustomSpeedDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Übernehmen", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomSpeedDialog = false }) {
                        Text("Abbrechen")
                    }
                }
            )
        }

        if (showEqualizerDialog) {
            EqualizerDialog(
                currentPreset = audioPreset,
                loudnessGainDb = loudnessGainMb / 100,
                isVolumeBoostEnabled = isVolumeBoostEnabled,
                isSkipSilenceEnabled = isSkipSilenceEnabled,
                onPresetSelected = { preset -> viewModel.setAudioPreset(preset) },
                onLoudnessGainChanged = { gainDb -> viewModel.setLoudnessGainDb(gainDb) },
                onVolumeBoostToggled = { viewModel.toggleVolumeBoost() },
                onSkipSilenceToggled = { viewModel.toggleSkipSilence() },
                onDismiss = { showEqualizerDialog = false }
            )
        }
    }
}

@Composable
private fun CoverArtWithGestures(
    coverUri: String?,
    filePath: String? = null,
    title: String?,
    isPlaying: Boolean,
    onDoubleTap: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    var totalDrag by remember { mutableStateOf(0f) }
    var feedbackText by remember { mutableStateOf<String?>(null) }

    val coverModel = remember(coverUri, filePath) {
        CoverHelper.resolveCoverModel(coverUri, filePath)
    }

    LaunchedEffect(feedbackText) {
        if (feedbackText != null) {
            delay(700L)
            feedbackText = null
        }
    }

    AmbientCoverGlow(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .pointerInput(isPlaying) {
                detectTapGestures(
                    onDoubleTap = {
                        feedbackText = if (isPlaying) "Pause" else "Play"
                        onDoubleTap()
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -40f) {
                            feedbackText = "+10s"
                            onSwipeLeft()
                        } else if (totalDrag > 40f) {
                            feedbackText = "-10s"
                            onSwipeRight()
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (coverModel != null) {
            AsyncImage(
                model = coverModel,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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

        // Visuelle Gesten-Rückmeldung Overlay Badge
        feedbackText?.let { text ->
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
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

@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier.size(width = 22.dp, height = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val heights = if (isPlaying) listOf(bar1Height, bar2Height, bar3Height) else listOf(0.4f, 0.7f, 0.4f)
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(h)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
