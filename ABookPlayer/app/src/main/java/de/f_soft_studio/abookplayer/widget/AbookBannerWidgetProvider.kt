package de.f_soft_studio.abookplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import de.f_soft_studio.abookplayer.MainActivity
import de.f_soft_studio.abookplayer.R
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.player.controller.PlaybackController
import de.f_soft_studio.abookplayer.storage.FolderScanner
import java.io.File

/**
 * Erweitertes ABook Banner-Widget mit 1-Minuten & 10-Sekunden Sprungtasten sowie geblürtem Full-Cover Hintergrund.
 */
class AbookBannerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return

        val controller = PlaybackController.getInstance(context)

        when (action) {
            ACTION_WIDGET_PLAY_PAUSE -> {
                if (controller.currentAudiobook.value == null) {
                    controller.loadLastPlayedAudiobook(autoPlay = true) {
                        updateAllWidgets(context)
                    }
                } else {
                    controller.togglePlayPause()
                    updateAllWidgets(context)
                }
            }
            ACTION_WIDGET_SKIP_BACK_10S -> {
                if (controller.currentAudiobook.value == null) {
                    controller.loadLastPlayedAudiobook(autoPlay = false) {
                        controller.skip10SecondsBackward()
                        updateAllWidgets(context)
                    }
                } else {
                    controller.skip10SecondsBackward()
                    updateAllWidgets(context)
                }
            }
            ACTION_WIDGET_SKIP_FWD_10S -> {
                if (controller.currentAudiobook.value == null) {
                    controller.loadLastPlayedAudiobook(autoPlay = false) {
                        controller.skip10SecondsForward()
                        updateAllWidgets(context)
                    }
                } else {
                    controller.skip10SecondsForward()
                    updateAllWidgets(context)
                }
            }
            ACTION_WIDGET_SKIP_BACK_1MIN -> {
                if (controller.currentAudiobook.value == null) {
                    controller.loadLastPlayedAudiobook(autoPlay = false) {
                        controller.skip60SecondsBackward()
                        updateAllWidgets(context)
                    }
                } else {
                    controller.skip60SecondsBackward()
                    updateAllWidgets(context)
                }
            }
            ACTION_WIDGET_SKIP_FWD_1MIN -> {
                if (controller.currentAudiobook.value == null) {
                    controller.loadLastPlayedAudiobook(autoPlay = false) {
                        controller.skip60SecondsForward()
                        updateAllWidgets(context)
                    }
                } else {
                    controller.skip60SecondsForward()
                    updateAllWidgets(context)
                }
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_PLAY_PAUSE = "de.f_soft_studio.abookplayer.widget.banner.PLAY_PAUSE"
        const val ACTION_WIDGET_SKIP_BACK_10S = "de.f_soft_studio.abookplayer.widget.banner.SKIP_BACK_10S"
        const val ACTION_WIDGET_SKIP_FWD_10S = "de.f_soft_studio.abookplayer.widget.banner.SKIP_FWD_10S"
        const val ACTION_WIDGET_SKIP_BACK_1MIN = "de.f_soft_studio.abookplayer.widget.banner.SKIP_BACK_1MIN"
        const val ACTION_WIDGET_SKIP_FWD_1MIN = "de.f_soft_studio.abookplayer.widget.banner.SKIP_FWD_1MIN"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_abook_banner)

            val controller = PlaybackController.getInstance(context)
            var audiobook = controller.currentAudiobook.value

            if (audiobook == null) {
                controller.loadLastPlayedAudiobook(autoPlay = false) {
                    updateAllWidgets(context)
                }
            }

            audiobook = controller.currentAudiobook.value
            val currentChapter = controller.currentChapter.value
            val isPlaying = controller.isPlaying.value
            val currentPos = controller.currentPosition.value
            val duration = controller.duration.value

            // Geblürtes Cover-Hintergrundbild setzen
            val blurredBitmap = loadAndBlurCover(audiobook, 6)
            if (blurredBitmap != null) {
                views.setImageViewBitmap(R.id.widget_bg_image, blurredBitmap)
            } else {
                views.setImageViewResource(R.id.widget_bg_image, 0)
            }

            views.setTextViewText(R.id.widget_title, audiobook?.title ?: "ABook Player")

            val subtitle = when {
                currentChapter != null && !currentChapter.title.isBlank() -> {
                    if (!audiobook?.author.isNullOrBlank()) {
                        "${audiobook?.author} • ${currentChapter.title}"
                    } else {
                        "Kapitel: ${currentChapter.title}"
                    }
                }
                audiobook != null && !audiobook.author.isBlank() -> audiobook.author
                else -> "Kein Hörbuch geladen"
            }
            views.setTextViewText(R.id.widget_author, subtitle)

            val progressText = if (duration > 0L) {
                val pct = (currentPos * 100 / duration).toInt()
                "$pct% (${formatTimeMs(currentPos)} / ${formatTimeMs(duration)})"
            } else {
                "Bereit"
            }
            views.setTextViewText(R.id.widget_progress_text, progressText)

            if (duration > 0L) {
                val progressRatio = ((currentPos * 1000L) / duration).toInt().coerceIn(0, 1000)
                views.setProgressBar(R.id.widget_progress_bar, 1000, progressRatio, false)
            } else {
                views.setProgressBar(R.id.widget_progress_bar, 1000, 0, false)
            }

            views.setImageViewResource(
                R.id.widget_btn_play_pause,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            // App öffnen bei Klick auf Titel oder Autor
            val appIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_title, appIntent)
            views.setOnClickPendingIntent(R.id.widget_author, appIntent)
            views.setOnClickPendingIntent(R.id.widget_progress_text, appIntent)
            views.setOnClickPendingIntent(R.id.widget_progress_bar, appIntent)

            // Broadcast Intents für alle Steuerungsknöpfe
            val playPauseIntent = PendingIntent.getBroadcast(
                context,
                20,
                Intent(context, AbookBannerWidgetProvider::class.java).apply { action = ACTION_WIDGET_PLAY_PAUSE },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPauseIntent)

            val skipBack10sIntent = PendingIntent.getBroadcast(
                context,
                21,
                Intent(context, AbookBannerWidgetProvider::class.java).apply { action = ACTION_WIDGET_SKIP_BACK_10S },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_skip_back_10s, skipBack10sIntent)

            val skipFwd10sIntent = PendingIntent.getBroadcast(
                context,
                22,
                Intent(context, AbookBannerWidgetProvider::class.java).apply { action = ACTION_WIDGET_SKIP_FWD_10S },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_skip_fwd_10s, skipFwd10sIntent)

            val skipBack1minIntent = PendingIntent.getBroadcast(
                context,
                23,
                Intent(context, AbookBannerWidgetProvider::class.java).apply { action = ACTION_WIDGET_SKIP_BACK_1MIN },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_skip_back_1min, skipBack1minIntent)

            val skipFwd1minIntent = PendingIntent.getBroadcast(
                context,
                24,
                Intent(context, AbookBannerWidgetProvider::class.java).apply { action = ACTION_WIDGET_SKIP_FWD_1MIN },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_skip_fwd_1min, skipFwd1minIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, AbookBannerWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            } catch (_: Exception) {}
        }

        private fun loadAndBlurCover(audiobook: Audiobook?, radius: Int): Bitmap? {
            if (audiobook == null) return null
            return try {
                var original: Bitmap? = null
                val coverUri = audiobook.coverUri
                if (!coverUri.isNullOrBlank()) {
                    val coverFile = File(coverUri)
                    if (coverFile.exists()) {
                        original = BitmapFactory.decodeFile(coverFile.absolutePath)
                    }
                }

                if (original == null && audiobook.filePath.isNotBlank()) {
                    val file = File(audiobook.filePath)
                    val targetAudioFile = if (file.isDirectory) {
                        file.listFiles()?.firstOrNull { FolderScanner.isAudioFile(it) }
                    } else if (file.exists()) {
                        file
                    } else null

                    if (targetAudioFile != null && targetAudioFile.exists()) {
                        try {
                            val retriever = android.media.MediaMetadataRetriever()
                            retriever.setDataSource(targetAudioFile.absolutePath)
                            val art = retriever.embeddedPicture
                            retriever.release()
                            if (art != null && art.isNotEmpty()) {
                                original = BitmapFactory.decodeByteArray(art, 0, art.size)
                            }
                        } catch (_: Exception) {}
                    }
                }

                if (original == null) return null

                // Downscale for smooth high-performance blur
                val small = Bitmap.createScaledBitmap(original, 100, 100, true)
                val blurred = fastBoxBlur(small, radius = radius.coerceIn(4, 10))
                Bitmap.createScaledBitmap(blurred, 300, 300, true)
            } catch (_: Exception) {
                null
            }
        }

        private fun fastBoxBlur(sentBitmap: Bitmap, radius: Int): Bitmap {
            val bitmap = sentBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val w = bitmap.width
            val h = bitmap.height
            val pix = IntArray(w * h)
            bitmap.getPixels(pix, 0, w, 0, 0, w, h)

            boxBlurHorizontal(pix, w, h, radius)
            boxBlurVertical(pix, w, h, radius)
            boxBlurHorizontal(pix, w, h, radius)
            boxBlurVertical(pix, w, h, radius)

            bitmap.setPixels(pix, 0, w, 0, 0, w, h)
            return bitmap
        }

        private fun boxBlurHorizontal(pixels: IntArray, w: Int, h: Int, radius: Int) {
            val div = radius + radius + 1
            for (y in 0 until h) {
                val lineStart = y * w
                var r = 0
                var g = 0
                var b = 0
                for (i in -radius..radius) {
                    val px = pixels[lineStart + i.coerceIn(0, w - 1)]
                    r += (px shr 16) and 0xFF
                    g += (px shr 8) and 0xFF
                    b += px and 0xFF
                }
                for (x in 0 until w) {
                    pixels[lineStart + x] = (0xFF shl 24) or ((r / div) shl 16) or ((g / div) shl 8) or (b / div)
                    val leftPx = pixels[lineStart + (x - radius).coerceIn(0, w - 1)]
                    val rightPx = pixels[lineStart + (x + radius + 1).coerceIn(0, w - 1)]
                    r += ((rightPx shr 16) and 0xFF) - ((leftPx shr 16) and 0xFF)
                    g += ((rightPx shr 8) and 0xFF) - ((leftPx shr 8) and 0xFF)
                    b += (rightPx and 0xFF) - (leftPx and 0xFF)
                }
            }
        }

        private fun boxBlurVertical(pixels: IntArray, w: Int, h: Int, radius: Int) {
            val div = radius + radius + 1
            for (x in 0 until w) {
                var r = 0
                var g = 0
                var b = 0
                for (i in -radius..radius) {
                    val px = pixels[i.coerceIn(0, h - 1) * w + x]
                    r += (px shr 16) and 0xFF
                    g += (px shr 8) and 0xFF
                    b += px and 0xFF
                }
                for (y in 0 until h) {
                    pixels[y * w + x] = (0xFF shl 24) or ((r / div) shl 16) or ((g / div) shl 8) or (b / div)
                    val topPx = pixels[(y - radius).coerceIn(0, h - 1) * w + x]
                    val bottomPx = pixels[(y + radius + 1).coerceIn(0, h - 1) * w + x]
                    r += ((bottomPx shr 16) and 0xFF) - ((topPx shr 16) and 0xFF)
                    g += ((bottomPx shr 8) and 0xFF) - ((topPx shr 8) and 0xFF)
                    b += (bottomPx and 0xFF) - (topPx and 0xFF)
                }
            }
        }

        private fun formatTimeMs(timeMs: Long): String {
            val totalSeconds = (timeMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val hours = minutes / 60
            val remainingMins = minutes % 60
            return if (hours > 0) {
                String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", hours, remainingMins, totalSeconds % 60)
            } else {
                String.format(java.util.Locale.getDefault(), "%02d:%02d", remainingMins, totalSeconds % 60)
            }
        }
    }
}
