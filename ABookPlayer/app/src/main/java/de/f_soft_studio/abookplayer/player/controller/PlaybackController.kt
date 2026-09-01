package de.f_soft_studio.abookplayer.player.controller

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import de.f_soft_studio.abookplayer.MainActivity
import de.f_soft_studio.abookplayer.R
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Chapter
import de.f_soft_studio.abookplayer.domain.usecase.SaveProgressUseCase
import de.f_soft_studio.abookplayer.player.service.AbookPlaybackService
import de.f_soft_studio.abookplayer.util.AudiobookMetadataText
import de.f_soft_studio.abookplayer.util.PlayableMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class PlaybackController(
    private val context: Context,
    private val saveProgressUseCase: SaveProgressUseCase
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastPauseTimestamp: Long = 0L

    private val _seekIncrementMs = MutableStateFlow(10_000L)
    val seekIncrementMs: StateFlow<Long> = _seekIncrementMs.asStateFlow()

    fun setSeekIncrement(incrementMs: Long) {
        _seekIncrementMs.value = incrementMs.coerceAtLeast(1000L)
    }

    private val player: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setSeekBackIncrementMs(_seekIncrementMs.value)
            .setSeekForwardIncrementMs(_seekIncrementMs.value)
            .build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    updatePlaybackServiceNotification()
                    if (isPlaying) {
                        startProgressTracker()
                        applyVolumeBoost()
                    } else {
                        stopProgressTracker()
                        saveCurrentProgress()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val book = _currentAudiobook.value
                        val bookDuration = book?.duration ?: 0L
                        if (bookDuration > 0L) {
                            _duration.value = bookDuration
                        } else if (_duration.value <= 0L && player.duration > 0L) {
                            _duration.value = player.duration
                        }
                        applyPlaybackSpeed()
                        applyVolumeBoost()
                        val pos = calculateGlobalPosition()
                        _currentPosition.value = pos
                        updateCurrentChapter(pos)
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    applyPlaybackSpeed()
                    applyVolumeBoost()
                    val pos = calculateGlobalPosition()
                    _currentPosition.value = pos
                    updateCurrentChapter(pos)
                    updatePlaybackServiceNotification()
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    val pos = calculateGlobalPosition()
                    _currentPosition.value = pos
                    updateCurrentChapter(pos)
                }

                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                    _playbackSpeed.value = playbackParameters.speed
                }
            })
        }
    }

    private var mediaSession: MediaLibrarySession? = null
    val loudnessController = LoudnessController()

    private var shakeDetector: de.f_soft_studio.abookplayer.util.ShakeDetector? = null

    init {
        instance = this
        try {
            val sessionActivityIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val customCmdSkipBack = SessionCommand(AbookPlaybackService.ACTION_SKIP_10_BACKWARD, android.os.Bundle.EMPTY)
            val customCmdSkipFwd = SessionCommand(AbookPlaybackService.ACTION_SKIP_10_FORWARD, android.os.Bundle.EMPTY)

            // Icons MÜSSEN App-eigene Ressourcen sein: Android Auto löst die Icon-Resource-ID
            // einer Custom-Action gegen die Ressourcen der Media-App auf. android.R.drawable.*
            // ist dort nicht auflösbar -> die Buttons würden in Android Auto verschwinden.
            val btnSkipBack = CommandButton.Builder()
                .setDisplayName("-10s")
                .setIconResId(R.drawable.ic_skip_back_10)
                .setSessionCommand(customCmdSkipBack)
                .build()

            val btnSkipFwd = CommandButton.Builder()
                .setDisplayName("+10s")
                .setIconResId(R.drawable.ic_skip_forward_10)
                .setSessionCommand(customCmdSkipFwd)
                .build()

            val libraryCallback = object : MediaLibrarySession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(customCmdSkipBack)
                        .add(customCmdSkipFwd)
                        .build()

                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(sessionCommands)
                        .setCustomLayout(listOf(btnSkipBack, btnSkipFwd))
                        .build()
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: android.os.Bundle
                ): ListenableFuture<SessionResult> {
                    when (customCommand.customAction) {
                        AbookPlaybackService.ACTION_SKIP_10_BACKWARD -> skip10SecondsBackward()
                        AbookPlaybackService.ACTION_SKIP_10_FORWARD -> skip10SecondsForward()
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }

            mediaSession = MediaLibrarySession.Builder(context, player, libraryCallback)
                .setSessionActivity(sessionActivityIntent)
                .build()
            activeMediaSession = mediaSession
        } catch (e: Exception) {
            android.util.Log.e("PlaybackController", "Fehler beim Erstellen der MediaLibrarySession: ${e.message}", e)
        }

        shakeDetector = de.f_soft_studio.abookplayer.util.ShakeDetector(context) {
            if (sleepTimerController.isActive.value && sleepTimerController.isShakeToResetEnabled.value) {
                sleepTimerController.extendTimerMinutes(15)
                try {
                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(200)
                    }
                } catch (_: Exception) {}
            }
        }
        shakeDetector?.startListening()

        scope.launch {
            sleepTimerController.volumeMultiplier.collect { mult ->
                try {
                    player.volume = mult
                } catch (_: Exception) {}
            }
        }
    }

    val sleepTimerController = SleepTimerController {
        pause()
    }

    private val _currentAudiobook = MutableStateFlow<Audiobook?>(null)
    val currentAudiobook: StateFlow<Audiobook?> = _currentAudiobook.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()

    /** Einmalige Wiedergabe-Fehlermeldungen für die UI (z. B. fehlende/verschobene Audiodateien). */
    private val _playbackError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val playbackError: SharedFlow<String> = _playbackError.asSharedFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isVolumeBoostEnabled = MutableStateFlow(false)
    val isVolumeBoostEnabled: StateFlow<Boolean> = _isVolumeBoostEnabled.asStateFlow()

    private val _isSkipSilenceEnabled = MutableStateFlow(false)
    val isSkipSilenceEnabled: StateFlow<Boolean> = _isSkipSilenceEnabled.asStateFlow()

    fun toggleSkipSilence() {
        setSkipSilenceEnabled(!_isSkipSilenceEnabled.value)
    }

    fun setSkipSilenceEnabled(enabled: Boolean) {
        _isSkipSilenceEnabled.value = enabled
        try {
            player.skipSilenceEnabled = enabled
        } catch (_: Exception) {}
    }

    private var progressJob: Job? = null

    private fun loadCoverBytes(coverFile: File?): ByteArray? {
        if (coverFile == null || !coverFile.exists()) return null
        return try {
            val bitmap = BitmapFactory.decodeFile(coverFile.absolutePath) ?: return null
            val maxDim = 512
            val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
                val (targetW, targetH) = if (aspect >= 1f) maxDim to (maxDim / aspect).toInt() else (maxDim * aspect).toInt() to maxDim
                Bitmap.createScaledBitmap(bitmap, targetW.coerceAtLeast(1), targetH.coerceAtLeast(1), true)
            } else bitmap

            val baos = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            baos.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Lädt ein Hörbuch und seine Kapitel-Playlist und startet optional sofort die Wiedergabe.
     */
    fun loadAudiobook(audiobook: Audiobook, chapterList: List<Chapter> = emptyList(), autoPlay: Boolean = false) {
        _currentAudiobook.value = audiobook
        _chapters.value = chapterList
        lastPauseTimestamp = 0L

        if (audiobook.customSpeed != null) {
            _playbackSpeed.value = audiobook.customSpeed
            applyPlaybackSpeed()
        }

        var calculatedDuration = audiobook.duration
        if (calculatedDuration <= 0L && chapterList.isNotEmpty()) {
            val lastChapter = chapterList.last()
            val lastChapterFile = lastChapter.audioPath?.let { File(it) }
            val lastChapterDur = if (lastChapterFile != null && lastChapterFile.exists()) {
                de.f_soft_studio.abookplayer.util.ChapterDurations.readDurationMs(lastChapterFile)
            } else 0L
            calculatedDuration = lastChapter.startTime + lastChapterDur
        }
        _duration.value = calculatedDuration

        val distinctAudioPaths = chapterList.mapNotNull { it.audioPath?.ifBlank { null } }.distinct()
        val isMultiFile = distinctAudioPaths.size > 1

        val coverFile = audiobook.coverUri?.let {
            val trimmed = it.trim()
            if (trimmed.startsWith("file://")) File(trimmed.removePrefix("file://")) else File(trimmed)
        }
        val artworkUri = when {
            audiobook.coverUri?.startsWith("content://") == true -> Uri.parse(audiobook.coverUri)
            coverFile != null && coverFile.exists() -> Uri.fromFile(coverFile)
            else -> null
        }
        val artworkBytes = de.f_soft_studio.abookplayer.util.CoverHelper.loadCoverBytes(audiobook.coverUri, audiobook.filePath)

        fun createMetadata(ch: Chapter? = null): MediaMetadata {
            val texts = AudiobookMetadataText.derive(
                bookTitle = audiobook.title,
                author = audiobook.author,
                chapterTitle = ch?.title
            )

            val builder = MediaMetadata.Builder()
                .setTitle(texts.title)
                .setDisplayTitle(texts.title)
                .setAlbumTitle(audiobook.title.ifBlank { texts.title })
                .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
                .setIsPlayable(true)
                .setIsBrowsable(false)

            texts.artist?.let {
                builder.setArtist(it)
                builder.setAlbumArtist(it)
            }
            texts.subtitle?.let { builder.setSubtitle(it) }

            if (artworkBytes != null) {
                builder.setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            }
            if (artworkUri != null) {
                builder.setArtworkUri(artworkUri)
            }
            return builder.build()
        }

        val mediaMetadata = createMetadata()
        val mediaItems = mutableListOf<MediaItem>()

        if (isMultiFile) {
            chapterList.forEach { ch ->
                val path = ch.audioPath
                if (PlayableMedia.isPlayableFile(path)) {
                    val chMetadata = createMetadata(ch)
                    mediaItems.add(
                        MediaItem.Builder()
                            .setUri(Uri.fromFile(File(path!!)))
                            .setMediaMetadata(chMetadata)
                            .build()
                    )
                }
            }
        } else {
            val singlePath = distinctAudioPaths.firstOrNull() ?: audiobook.filePath
            if (PlayableMedia.isPlayableFile(singlePath)) {
                mediaItems.add(
                    MediaItem.Builder()
                        .setUri(Uri.fromFile(File(singlePath)))
                        .setMediaMetadata(mediaMetadata)
                        .build()
                )
            }
        }

        // Letzter Fallback: direkter Buchpfad – aber nur, wenn es eine echte Datei ist.
        // Ein Verzeichnis (z. B. ein imported_*-Ordner ohne Dateien) würde ExoPlayer
        // mit "EISDIR (Is a directory)" abbrechen lassen; die Wiedergabe wirkt dann tot.
        if (mediaItems.isEmpty() && PlayableMedia.isPlayableFile(audiobook.filePath)) {
            mediaItems.add(
                MediaItem.Builder()
                    .setUri(Uri.fromFile(File(audiobook.filePath)))
                    .setMediaMetadata(mediaMetadata)
                    .build()
            )
        }

        if (mediaItems.isNotEmpty()) {
            player.setMediaItems(mediaItems)
            player.prepare()
            applyPlaybackSpeed()
            applyVolumeBoost()
            seekTo(audiobook.currentPosition)
            if (autoPlay) {
                play()
            }
        } else {
            // Keine abspielbare Datei gefunden (verschoben/gelöscht oder Pfad ist ein Ordner).
            _playbackError.tryEmit(
                "Keine abspielbaren Audiodateien für „${audiobook.title}“ gefunden. " +
                    "Die Dateien wurden vermutlich verschoben oder gelöscht – bitte das Hörbuch neu importieren."
            )
        }

        updateCurrentChapter(audiobook.currentPosition)
        updatePlaybackServiceNotification()
    }

    /**
     * Schaltet zwischen Wiedergabe und Pause um.
     */
    fun togglePlayPause() {
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    /**
     * Startet die Wiedergabe, prüft auf automatischen Rücksprung bei Hörpause und aktiviert den Vordergrunddienst.
     */
    fun play() {
        if (lastPauseTimestamp > 0L) {
            val pausedDurationMs = System.currentTimeMillis() - lastPauseTimestamp
            val pausedMinutes = pausedDurationMs / (1000L * 60L)

            val rewindSeconds = when {
                pausedMinutes >= 120L -> 30L
                pausedMinutes >= 30L -> 20L
                pausedMinutes >= 5L -> 10L
                else -> 0L
            }

            if (rewindSeconds > 0L) {
                val currentPos = calculateGlobalPosition()
                val newPos = (currentPos - (rewindSeconds * 1000L)).coerceAtLeast(0L)
                seekTo(newPos)
            }
            lastPauseTimestamp = 0L
        }

        startPlaybackService(AbookPlaybackService.ACTION_START)
        player.play()
    }

    /**
     * Pausiert die Wiedergabe, speichert den Zeitstempel für automatischen Rücksprung und sichert den Fortschritt.
     */
    fun pause() {
        lastPauseTimestamp = System.currentTimeMillis()
        player.pause()
        saveCurrentProgress()
        updatePlaybackServiceNotification()
    }

    private fun startPlaybackService(action: String = AbookPlaybackService.ACTION_UPDATE_NOTIFICATION) {
        try {
            val intent = Intent(context, AbookPlaybackService::class.java).apply {
                this.action = action
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {}
    }

    private fun updatePlaybackServiceNotification() {
        startPlaybackService(AbookPlaybackService.ACTION_UPDATE_NOTIFICATION)
        notifyWidgetUpdate()
    }

    private fun notifyWidgetUpdate() {
        try {
            de.f_soft_studio.abookplayer.widget.AbookWidgetProvider.updateAllWidgets(context)
            de.f_soft_studio.abookplayer.widget.AbookBannerWidgetProvider.updateAllWidgets(context)
        } catch (_: Exception) {}
    }

    /**
     * Springt entsprechend dem eingestellten Intervall vorwärts.
     */
    fun skip10SecondsForward() {
        val currentPos = calculateGlobalPosition()
        val inc = _seekIncrementMs.value
        val newPos = (currentPos + inc).coerceAtMost(_duration.value.coerceAtLeast(0L))
        seekTo(newPos)
    }

    /**
     * Springt entsprechend dem eingestellten Intervall zurück.
     */
    fun skip10SecondsBackward() {
        val currentPos = calculateGlobalPosition()
        val inc = _seekIncrementMs.value
        val newPos = (currentPos - inc).coerceAtLeast(0L)
        seekTo(newPos)
    }

    /**
     * Springt 30 Sekunden vorwärts.
     */
    fun skip30SecondsForward() {
        val currentPos = calculateGlobalPosition()
        val newPos = (currentPos + 30000L).coerceAtMost(_duration.value.coerceAtLeast(0L))
        seekTo(newPos)
    }

    /**
     * Springt 1 Minute (60 Sekunden) vorwärts.
     */
    fun skip60SecondsForward() {
        val currentPos = calculateGlobalPosition()
        val newPos = (currentPos + 60000L).coerceAtMost(_duration.value.coerceAtLeast(0L))
        seekTo(newPos)
    }

    /**
     * Springt 1 Minute (60 Sekunden) zurück.
     */
    fun skip60SecondsBackward() {
        val currentPos = calculateGlobalPosition()
        val newPos = (currentPos - 60000L).coerceAtLeast(0L)
        seekTo(newPos)
    }

    /**
     * Springt zum nächsten Kapitel.
     */
    fun skipToNextChapter() {
        val list = _chapters.value
        if (list.isEmpty()) return
        val currentPos = calculateGlobalPosition()
        val nextChapter = list.firstOrNull { it.startTime > currentPos + 1000L } ?: list.last()
        seekTo(nextChapter.startTime)
    }

    /**
     * Springt zum vorherigen Kapitel oder zum Start des aktuellen Kapitels.
     */
    fun skipToPreviousChapter() {
        val list = _chapters.value
        if (list.isEmpty()) return
        val currentPos = calculateGlobalPosition()
        val currentCh = list.lastOrNull { currentPos >= it.startTime } ?: list.first()
        if (currentPos - currentCh.startTime > 3000L) {
            seekTo(currentCh.startTime)
        } else {
            val currentIndex = list.indexOf(currentCh)
            val prevChapter = list.getOrNull(currentIndex - 1) ?: list.first()
            seekTo(prevChapter.startTime)
        }
    }

    /**
     * Springt an eine genaue globale Position in Millisekunden.
     */
    fun seekTo(positionMs: Long) {
        val chapterList = _chapters.value
        val clampedPos = positionMs.coerceIn(0L, _duration.value.coerceAtLeast(0L))
        _currentPosition.value = clampedPos

        val distinctAudioPaths = chapterList.mapNotNull { it.audioPath?.ifBlank { null } }.distinct()
        val isMultiFile = distinctAudioPaths.size > 1

        if (isMultiFile && chapterList.isNotEmpty()) {
            val matchedIndex = chapterList.indexOfLast { clampedPos >= it.startTime }.coerceAtLeast(0)
            val matchedChapter = chapterList.getOrNull(matchedIndex)
            if (matchedChapter != null && matchedIndex < player.mediaItemCount) {
                val offsetInItem = (clampedPos - matchedChapter.startTime).coerceAtLeast(0L)
                player.seekTo(matchedIndex, offsetInItem)
            } else {
                player.seekTo(clampedPos)
            }
        } else {
            player.seekTo(clampedPos)
        }

        updateCurrentChapter(clampedPos)
        saveCurrentProgress()
        notifyWidgetUpdate()
    }

    /**
     * Passt die Wiedergabegeschwindigkeit an (z. B. 0.8x, 1.0x, 1.25x, 1.5x, 2.0x).
     */
    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyPlaybackSpeed()
        _currentAudiobook.value?.let { book ->
            val updated = book.copy(customSpeed = speed)
            _currentAudiobook.value = updated
            scope.launch {
                try {
                    val db = de.f_soft_studio.abookplayer.data.local.db.AbookDatabase.getInstance(context)
                    val repo = de.f_soft_studio.abookplayer.data.repository.AudiobookRepository(
                        audiobookDao = db.audiobookDao(),
                        chapterDao = db.chapterDao(),
                        bookmarkDao = db.bookmarkDao(),
                        listeningSessionDao = db.listeningSessionDao(),
                        characterDao = db.characterDao()
                    )
                    repo.saveAudiobook(updated)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Aktiviert oder deaktiviert den Lautstärke-Boost (LoudnessEnhancer).
     */
    fun toggleVolumeBoost() {
        _isVolumeBoostEnabled.value = !_isVolumeBoostEnabled.value
        applyVolumeBoost()
    }

    private fun applyVolumeBoost() {
        try {
            val audioSessionId = player.audioSessionId
            if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId > 0) {
                loudnessController.attachAudioSession(audioSessionId)
                loudnessController.setLoudnessEnabled(_isVolumeBoostEnabled.value)
            }
        } catch (_: Exception) {}
    }

    private fun applyPlaybackSpeed() {
        try {
            val speed = _playbackSpeed.value
            val currentSpeed = player.playbackParameters.speed
            if (currentSpeed != speed) {
                player.playbackParameters = PlaybackParameters(speed)
            }
        } catch (_: Exception) {}
    }

    private fun calculateGlobalPosition(): Long {
        return try {
            val chapterList = _chapters.value
            if (chapterList.isEmpty()) {
                player.currentPosition.coerceAtLeast(0L)
            } else {
                val distinctAudioPaths = chapterList.mapNotNull { it.audioPath?.ifBlank { null } }.distinct()
                val isMultiFile = distinctAudioPaths.size > 1

                if (isMultiFile) {
                    val currentItemIndex = player.currentMediaItemIndex
                    val currentChapter = chapterList.getOrNull(currentItemIndex)
                    val offsetInItem = player.currentPosition.coerceAtLeast(0L)
                    (currentChapter?.startTime ?: 0L) + offsetInItem
                } else {
                    player.currentPosition.coerceAtLeast(0L)
                }
            }
        } catch (_: Exception) {
            _currentPosition.value
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var tickCount = 0
            while (_isPlaying.value) {
                try {
                    val pos = calculateGlobalPosition()
                    _currentPosition.value = pos
                    updateCurrentChapter(pos)
                    tickCount++
                    if (tickCount % 2 == 0) {
                        notifyWidgetUpdate()
                    }
                } catch (_: Exception) {}
                delay(500L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateCurrentChapter(positionMs: Long) {
        val list = _chapters.value
        if (list.isEmpty()) {
            _currentChapter.value = null
            return
        }
        val match = list.lastOrNull { positionMs >= it.startTime } ?: list.firstOrNull()
        val oldChapter = _currentChapter.value
        if (oldChapter != match) {
            _currentChapter.value = match
            updatePlaybackServiceNotification()
            if (oldChapter != null && match != null && oldChapter != match) {
                if (sleepTimerController.stopAtEndOfChapter.value) {
                    sleepTimerController.triggerChapterEndExpired()
                }
            }
        }
    }

    private fun saveCurrentProgress() {
        val book = _currentAudiobook.value ?: return
        val pos = calculateGlobalPosition()
        scope.launch {
            try {
                saveProgressUseCase(book.id, pos)
                val webDavSyncManager = de.f_soft_studio.abookplayer.storage.sync.WebDavSyncManager(context)
                if (webDavSyncManager.isConfigured && webDavSyncManager.isAutoSyncEnabled) {
                    val db = de.f_soft_studio.abookplayer.data.local.db.AbookDatabase.getInstance(context)
                    val repo = de.f_soft_studio.abookplayer.data.repository.AudiobookRepository(
                        audiobookDao = db.audiobookDao(),
                        chapterDao = db.chapterDao(),
                        bookmarkDao = db.bookmarkDao(),
                        listeningSessionDao = db.listeningSessionDao(),
                        characterDao = db.characterDao()
                    )
                    val syncUseCase = de.f_soft_studio.abookplayer.domain.usecase.SyncProgressUseCase(repo, webDavSyncManager)
                    syncUseCase()
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Lädt das zuletzt abgespielte Hörbuch aus der Datenbank, falls noch kein Hörbuch geladen ist.
     */
    fun loadLastPlayedAudiobook(
        autoPlay: Boolean = false,
        onLoaded: (() -> Unit)? = null
    ) {
        if (_currentAudiobook.value != null) {
            onLoaded?.invoke()
            return
        }
        scope.launch {
            try {
                val db = de.f_soft_studio.abookplayer.data.local.db.AbookDatabase.getInstance(context)
                val repository = de.f_soft_studio.abookplayer.data.repository.AudiobookRepository(
                    audiobookDao = db.audiobookDao(),
                    chapterDao = db.chapterDao(),
                    bookmarkDao = db.bookmarkDao(),
                    listeningSessionDao = db.listeningSessionDao()
                )
                val allBooks = repository.getAllAudiobooks().firstOrNull()
                val lastBook = allBooks?.maxByOrNull { it.lastPlayed } ?: allBooks?.firstOrNull()
                if (lastBook != null) {
                    val chapters = repository.getChaptersForAudiobook(lastBook.id).firstOrNull() ?: emptyList()
                    loadAudiobook(lastBook, chapters, autoPlay = autoPlay)
                }
            } catch (_: Exception) {}
            onLoaded?.invoke()
        }
    }

    /**
     * Gibt Ressourcen des ExoPlayers, des LoudnessEnhancers und der MediaSession frei.
     */
    fun release() {
        stopProgressTracker()
        try {
            loudnessController.release()
        } catch (_: Exception) {}
        try {
            startPlaybackService(AbookPlaybackService.ACTION_STOP)
        } catch (_: Exception) {}
        try {
            mediaSession?.release()
        } catch (_: Exception) {}
        activeMediaSession = null
        if (instance == this) {
            instance = null
        }
        try {
            player.release()
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile
        var instance: PlaybackController? = null
        var activeMediaSession: MediaSession? = null

        fun getInstance(context: Context): PlaybackController {
            return instance ?: synchronized(this) {
                instance ?: createInstance(context).also { instance = it }
            }
        }

        private fun createInstance(context: Context): PlaybackController {
            val appContext = context.applicationContext
            val db = de.f_soft_studio.abookplayer.data.local.db.AbookDatabase.getInstance(appContext)
            val repository = de.f_soft_studio.abookplayer.data.repository.AudiobookRepository(
                audiobookDao = db.audiobookDao(),
                chapterDao = db.chapterDao(),
                bookmarkDao = db.bookmarkDao(),
                listeningSessionDao = db.listeningSessionDao()
            )
            val recordListeningTimeUseCase = de.f_soft_studio.abookplayer.domain.usecase.RecordListeningTimeUseCase(repository)
            val saveProgressUseCase = de.f_soft_studio.abookplayer.domain.usecase.SaveProgressUseCase(repository, recordListeningTimeUseCase)

            return PlaybackController(appContext, saveProgressUseCase)
        }
    }
}

