package de.f_soft_studio.abookplayer.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File

/**
 * LibraryLocationManager: Verwaltet den zentralen öffentlichen Bibliotheksordner der Hörbuch-App.
 *
 * Struktur:
 * [Bibliotheksordner] (z. B. /Audiobooks, /Download/ABookPlayer)
 * ├── Hörbuch 1/
 * ├── Hörbuch 2.abook
 * └── .abooklib/
 *     ├── .nomedia
 *     ├── abook_database.db
 *     ├── covers/
 *     └── metadata/
 *
 * Temporäre Dateien (z. B. Entpack-Caches) verbleiben strikt im internen App-Cache-Ordner (context.cacheDir).
 */
object LibraryLocationManager {

    private const val PREFS_NAME = "abook_library_location_prefs"
    private const val KEY_LIBRARY_PATH = "library_base_path"
    private const val KEY_LIBRARY_URI = "library_base_uri"
    const val ABOOK_LIB_FOLDER = ".abooklib"

    /**
     * Ermittelt den aktuellen Bibliotheks-Hauptordner.
     * Falls noch keiner konfiguriert wurde, wird ein Android-Standard festgelegt.
     */
    fun getLibraryDir(context: Context): File {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customPath = prefs.getString(KEY_LIBRARY_PATH, null)

        if (!customPath.isNullOrBlank()) {
            val customDir = File(customPath)
            if (customDir.exists() || customDir.mkdirs()) {
                initAbookLibStructure(customDir)
                return customDir
            }
        }

        // 1. Android Standard: Audiobooks Ordner im externen Speicher
        val defaultAudiobooks = File(Environment.getExternalStorageDirectory(), "Audiobooks")
        if (defaultAudiobooks.exists() || defaultAudiobooks.mkdirs()) {
            initAbookLibStructure(defaultAudiobooks)
            return defaultAudiobooks
        }

        // 2. Fallback: Downloads/ABookPlayer
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val abookPlayerDownloads = File(downloadDir, "ABookPlayer")
        if (abookPlayerDownloads.exists() || abookPlayerDownloads.mkdirs()) {
            initAbookLibStructure(abookPlayerDownloads)
            return abookPlayerDownloads
        }

        // 3. Letzter Fallback: App-internes Hörbuch-Verzeichnis
        val internalDir = File(context.filesDir, "audiobooks").apply { if (!exists()) mkdirs() }
        initAbookLibStructure(internalDir)
        return internalDir
    }

    /**
     * Speichert einen neuen Bibliotheks-Pfad.
     */
    fun setLibraryDir(context: Context, dir: File, uri: Uri? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit().putString(KEY_LIBRARY_PATH, dir.absolutePath)
        if (uri != null) {
            editor.putString(KEY_LIBRARY_URI, uri.toString())
        }
        editor.apply()
        initAbookLibStructure(dir)
    }

    /**
     * Liefert den .abooklib-Unterordner innerhalb des Bibliotheksordners.
     */
    fun getAbookLibDir(context: Context): File {
        val base = getLibraryDir(context)
        return File(base, ABOOK_LIB_FOLDER).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Liefert den Pfad zur SQLite-Datenbankdatei in .abooklib.
     */
    fun getDatabaseFile(context: Context): File {
        val libDir = getAbookLibDir(context)
        return File(libDir, "abook_database.db")
    }

    /**
     * Liefert den Cover-Ordner in .abooklib/covers (mit sicherem internem Fallback).
     */
    fun getCoversDir(context: Context): File {
        return try {
            val libDir = getAbookLibDir(context)
            val covers = File(libDir, "covers")
            if (covers.exists() || covers.mkdirs()) {
                covers
            } else {
                File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
            }
        } catch (_: Exception) {
            File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
        }
    }

    /**
     * Liefert den Metadaten-Ordner in .abooklib/metadata (mit sicherem internem Fallback).
     */
    fun getMetadataDir(context: Context): File {
        return try {
            val libDir = getAbookLibDir(context)
            val meta = File(libDir, "metadata")
            if (meta.exists() || meta.mkdirs()) {
                meta
            } else {
                File(context.filesDir, "metadata").apply { if (!exists()) mkdirs() }
            }
        } catch (_: Exception) {
            File(context.filesDir, "metadata").apply { if (!exists()) mkdirs() }
        }
    }


    /**
     * Liefert einen sicheren temporären Cache-Ordner im internen App-Speicher (context.cacheDir).
     * Temporäre Daten werden niemals im öffentlichen Ordner abgelegt.
     */
    fun getTempDir(context: Context): File {
        return File(context.cacheDir, "temp_unpacked").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Bereinigt alle temporären Entpackungs- und Cache-Dateien im internen Speicher.
     */
    fun clearTempDir(context: Context) {
        try {
            val tempDir = File(context.cacheDir, "temp_unpacked")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e("LibraryLocationManager", "Fehler beim Bereinigen des temporären Cache: ${e.message}")
        }
    }

    /**
     * Initialisiert die .abooklib-Struktur inklusive .nomedia Datei.
     */
    private fun initAbookLibStructure(baseDir: File) {
        try {
            val abookLib = File(baseDir, ABOOK_LIB_FOLDER)
            if (!abookLib.exists()) {
                abookLib.mkdirs()
            }
            val nomedia = File(abookLib, ".nomedia")
            if (!nomedia.exists()) {
                nomedia.createNewFile()
            }
            val covers = File(abookLib, "covers")
            if (!covers.exists()) {
                covers.mkdirs()
            }
            val meta = File(abookLib, "metadata")
            if (!meta.exists()) {
                meta.mkdirs()
            }
        } catch (e: Exception) {
            Log.w("LibraryLocationManager", "Konnte .abooklib-Struktur nicht anlegen: ${e.message}")
        }
    }
}
