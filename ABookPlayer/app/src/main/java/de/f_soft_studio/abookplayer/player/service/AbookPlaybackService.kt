package de.f_soft_studio.abookplayer.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaStyleNotificationHelper
import de.f_soft_studio.abookplayer.MainActivity
import de.f_soft_studio.abookplayer.R
import de.f_soft_studio.abookplayer.player.controller.PlaybackController
import java.io.File

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

/**
 * Robustes MediaLibraryService & Foreground-Service für die Hintergrund- und Sperrbildschirm-Audiowiedergabe
 * sowie Android Auto Browser-Steuerung.
 */
class AbookPlaybackService : MediaLibraryService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        val activeSession = PlaybackController.activeMediaSession
        return activeSession as? MediaLibrarySession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_PAUSE -> PlaybackController.instance?.pause()
            ACTION_PLAY -> PlaybackController.instance?.play()
            ACTION_TOGGLE_PLAY_PAUSE -> PlaybackController.instance?.togglePlayPause()
            ACTION_SKIP_10_BACKWARD -> PlaybackController.instance?.skip10SecondsBackward()
            ACTION_SKIP_10_FORWARD -> PlaybackController.instance?.skip10SecondsForward()
            ACTION_SKIP_NEXT_CHAPTER -> PlaybackController.instance?.skipToNextChapter()
            ACTION_STOP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        try {
            de.f_soft_studio.abookplayer.widget.AbookWidgetProvider.updateAllWidgets(this)
        } catch (_: Exception) {}

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent) ?: null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                CHANNEL_ID,
                "ABook Player Wiedergabe",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Aktive Hörbuch-Wiedergabesteuerung"
                setShowBadge(false)
            }
            nm?.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val controller = PlaybackController.instance
        val audiobook = controller?.currentAudiobook?.value
        val currentChapter = controller?.currentChapter?.value
        val isPlaying = controller?.isPlaying?.value ?: false

        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val skipBackPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, AbookPlaybackService::class.java).apply { action = ACTION_SKIP_10_BACKWARD },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, AbookPlaybackService::class.java).apply { action = ACTION_TOGGLE_PLAY_PAUSE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val skipForwardPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, AbookPlaybackService::class.java).apply { action = ACTION_SKIP_10_FORWARD },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val titleText = audiobook?.title ?: "ABook Player"
        val subtitleText = currentChapter?.title?.let { "Kapitel: $it" }
            ?: audiobook?.author?.ifBlank { "Hörbuch-Wiedergabe aktiv" }
            ?: "Hörbuch-Wiedergabe aktiv"

        val coverBitmap = loadCoverBitmap(audiobook?.coverUri)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titleText)
            .setContentText(subtitleText)
            .setLargeIcon(coverBitmap)
            .setContentIntent(mainPendingIntent)
            .setOngoing(isPlaying)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_rew, "-10s", skipBackPendingIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(android.R.drawable.ic_media_ff, "+10s", skipForwardPendingIntent)

        val session = PlaybackController.activeMediaSession
        if (session != null) {
            val mediaStyle = MediaStyleNotificationHelper.MediaStyle(session)
                .setShowActionsInCompactView(0, 1, 2)
            builder.setStyle(mediaStyle)
        }

        return builder.build()
    }

    private fun loadCoverBitmap(coverUri: String?): Bitmap? {
        if (coverUri.isNullOrBlank()) return null
        return try {
            val file = File(coverUri)
            if (!file.exists()) return null
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        const val CHANNEL_ID = "abook_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "de.f_soft_studio.abookplayer.action.START"
        const val ACTION_PLAY = "de.f_soft_studio.abookplayer.action.PLAY"
        const val ACTION_PAUSE = "de.f_soft_studio.abookplayer.action.PAUSE"
        const val ACTION_TOGGLE_PLAY_PAUSE = "de.f_soft_studio.abookplayer.action.TOGGLE"
        const val ACTION_SKIP_10_BACKWARD = "de.f_soft_studio.abookplayer.action.SKIP_BACK"
        const val ACTION_SKIP_10_FORWARD = "de.f_soft_studio.abookplayer.action.SKIP_FWD"
        const val ACTION_SKIP_NEXT_CHAPTER = "de.f_soft_studio.abookplayer.action.SKIP_NEXT"
        const val ACTION_UPDATE_NOTIFICATION = "de.f_soft_studio.abookplayer.action.UPDATE"
        const val ACTION_STOP = "de.f_soft_studio.abookplayer.action.STOP"
    }
}
