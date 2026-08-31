package de.f_soft_studio.abookplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

/**
 * Universeller Helfer zum Laden und Auflösen von Cover-Bildern.
 * Unterstützt Datei-Pfade, file://-URIs, content://-URIs sowie automatisches Fallback
 * auf eingebettete Cover (MediaMetadataRetriever) oder Ordner-Cover (cover.jpg/folder.jpg).
 */
object CoverHelper {

    /**
     * Ermittelt ein für Coil (AsyncImage) geeignetes Model-Objekt (File, Uri oder ByteArray).
     */
    fun resolveCoverModel(coverUri: String?, filePath: String? = null): Any? {
        if (!coverUri.isNullOrBlank()) {
            val trimmed = coverUri.trim()
            when {
                trimmed.startsWith("content://") || trimmed.startsWith("http://") || trimmed.startsWith("https://") -> {
                    return Uri.parse(trimmed)
                }
                trimmed.startsWith("file://") -> {
                    val rawPath = trimmed.removePrefix("file://")
                    val file = File(rawPath)
                    return if (file.exists()) file else Uri.parse(trimmed)
                }
                else -> {
                    val file = File(trimmed)
                    if (file.exists()) return file
                }
            }
        }

        // Fallback: Eingebettetes Cover aus Audiodatei oder Verzeichnis ermitteln
        if (!filePath.isNullOrBlank()) {
            try {
                val f = File(filePath)
                if (f.exists()) {
                    if (f.isDirectory) {
                        val img = f.listFiles()?.firstOrNull { file ->
                            val name = file.name.lowercase()
                            (name.startsWith("cover") || name.startsWith("folder") || name.startsWith("front")) &&
                                    (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp"))
                        }
                        if (img != null && img.exists()) return img
                    } else {
                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(f.absolutePath)
                        val embedded = mmr.embeddedPicture
                        mmr.release()
                        if (embedded != null && embedded.isNotEmpty()) {
                            return embedded
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        return null
    }

    /**
     * Lädt das Cover als Bitmap für Benachrichtigungen oder Widgets.
     */
    fun loadCoverBitmap(context: Context? = null, coverUri: String?, filePath: String? = null, inSampleSize: Int = 2): Bitmap? {
        if (!coverUri.isNullOrBlank()) {
            val trimmed = coverUri.trim()
            try {
                if (trimmed.startsWith("content://") && context != null) {
                    context.contentResolver.openInputStream(Uri.parse(trimmed))?.use { input ->
                        val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
                        return BitmapFactory.decodeStream(input, null, options)
                    }
                } else {
                    val rawPath = if (trimmed.startsWith("file://")) trimmed.removePrefix("file://") else trimmed
                    val file = File(rawPath)
                    if (file.exists()) {
                        val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
                        val bmp = BitmapFactory.decodeFile(file.absolutePath, options)
                        if (bmp != null) return bmp
                    }
                }
            } catch (_: Exception) {}
        }

        // Fallback: Audio-Datei
        if (!filePath.isNullOrBlank()) {
            try {
                val f = File(filePath)
                if (f.exists()) {
                    if (f.isDirectory) {
                        val img = f.listFiles()?.firstOrNull { file ->
                            val name = file.name.lowercase()
                            (name.startsWith("cover") || name.startsWith("folder") || name.startsWith("front")) &&
                                    (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp"))
                        }
                        if (img != null && img.exists()) {
                            val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
                            return BitmapFactory.decodeFile(img.absolutePath, options)
                        }
                    } else {
                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(f.absolutePath)
                        val embedded = mmr.embeddedPicture
                        mmr.release()
                        if (embedded != null && embedded.isNotEmpty()) {
                            val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
                            return BitmapFactory.decodeByteArray(embedded, 0, embedded.size, options)
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        return null
    }

    /**
     * Lädt das Cover als komprimiertes JPEG-Byte-Array für MediaMetadata / MediaSession / Android Auto.
     */
    fun loadCoverBytes(coverUri: String?, filePath: String? = null, maxDim: Int = 512): ByteArray? {
        val bitmap = loadCoverBitmap(null, coverUri, filePath, inSampleSize = 1) ?: return null
        return try {
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
}
