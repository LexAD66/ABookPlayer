package de.f_soft_studio.abookplayer.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

import android.net.Uri
import de.f_soft_studio.abookplayer.storage.AbookStorage
import de.f_soft_studio.abookplayer.ui.theme.AppThemeMode

/**
 * ViewModel für den Einstellungsbildschirm.
 * Verwaltet App-Cache-Größe, Sprungweiten und Einstellungen.
 */
class SettingsViewModel(
    private val context: Context,
    private val abookStorage: AbookStorage
) : ViewModel() {

    private val _cacheSizeBytes = MutableStateFlow(0L)
    val cacheSizeBytes: StateFlow<Long> = _cacheSizeBytes.asStateFlow()

    private val _skipDurationSeconds = MutableStateFlow(10)
    val skipDurationSeconds: StateFlow<Int> = _skipDurationSeconds.asStateFlow()

    private val _autoRewindSeconds = MutableStateFlow(10)
    val autoRewindSeconds: StateFlow<Int> = _autoRewindSeconds.asStateFlow()

    private val _appThemeMode = MutableStateFlow(AppThemeMode.DARK_STAGE)
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    private val _scannedFolders = MutableStateFlow<List<String>>(emptyList())
    val scannedFolders: StateFlow<List<String>> = _scannedFolders.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val prefs = context.getSharedPreferences("abook_settings_prefs", Context.MODE_PRIVATE)

    private val _isAutoOnlineCoverEnabled = MutableStateFlow(
        prefs.getBoolean("auto_online_cover_fetch", true)
    )
    val isAutoOnlineCoverEnabled: StateFlow<Boolean> = _isAutoOnlineCoverEnabled.asStateFlow()

    private val _currentLibraryPath = MutableStateFlow(
        de.f_soft_studio.abookplayer.storage.LibraryLocationManager.getLibraryDir(context).absolutePath
    )
    val currentLibraryPath: StateFlow<String> = _currentLibraryPath.asStateFlow()

    private val _isPublicStorageEnabled = MutableStateFlow(true)
    val isPublicStorageEnabled: StateFlow<Boolean> = _isPublicStorageEnabled.asStateFlow()

    private val _publicStorageFolderUri = MutableStateFlow(
        prefs.getString("public_storage_folder_uri", null)
    )
    val publicStorageFolderUri: StateFlow<String?> = _publicStorageFolderUri.asStateFlow()

    fun setAutoOnlineCoverEnabled(enabled: Boolean) {
        _isAutoOnlineCoverEnabled.value = enabled
        prefs.edit().putBoolean("auto_online_cover_fetch", enabled).apply()
        _statusMessage.value = if (enabled) "Automatische Online-Cover-Suche aktiviert" else "Automatische Online-Cover-Suche deaktiviert"
    }

    fun setLibraryLocation(dir: File, uri: Uri? = null) {
        de.f_soft_studio.abookplayer.storage.LibraryLocationManager.setLibraryDir(context, dir, uri)
        _currentLibraryPath.value = dir.absolutePath
        if (uri != null) {
            _publicStorageFolderUri.value = uri.toString()
            prefs.edit().putString("public_storage_folder_uri", uri.toString()).apply()
        }
        _statusMessage.value = "Bibliotheksordner geändert auf: ${dir.name}"
        triggerLibraryMigration()
    }

    fun setPublicStorageFolderUri(folderUri: String) {
        _publicStorageFolderUri.value = folderUri
        _isPublicStorageEnabled.value = true
        prefs.edit()
            .putBoolean("public_storage_enabled", true)
            .putString("public_storage_folder_uri", folderUri)
            .apply()
        try {
            val uri = Uri.parse(folderUri)
            if (uri.scheme == "content" && uri.authority == "com.android.externalstorage.documents") {
                val docId = android.provider.DocumentsContract.getTreeDocumentId(uri)
                val split = docId.split(":")
                val dir = if (split.size >= 2 && split[0].equals("primary", ignoreCase = true)) {
                    File(android.os.Environment.getExternalStorageDirectory(), split[1])
                } else if (split.size >= 2) {
                    File("/storage/${split[0]}", split[1])
                } else null
                if (dir != null && (dir.exists() || dir.mkdirs())) {
                    setLibraryLocation(dir, uri)
                    return
                }
            }
        } catch (_: Exception) {}
        triggerLibraryMigration()
    }

    fun triggerLibraryMigration() {
        viewModelScope.launch {
            _statusMessage.value = "Aktualisiere Bibliothek..."
            val targetDir = de.f_soft_studio.abookplayer.storage.LibraryLocationManager.getLibraryDir(context)
            _currentLibraryPath.value = targetDir.absolutePath
            val imported = abookStorage.scanAndImport()
            _statusMessage.value = "Bibliothek aktualisiert (${imported.size} Hörbücher gefunden)"
        }
    }

    init {
        calculateCacheSize()
        loadScannedFolders()
    }

    private fun loadScannedFolders() {
        val libDir = de.f_soft_studio.abookplayer.storage.LibraryLocationManager.getLibraryDir(context).absolutePath
        _scannedFolders.value = listOf(libDir)
    }

    fun addScannedFolder(path: String) {
        val current = _scannedFolders.value.toMutableList()
        if (!current.contains(path)) {
            current.add(path)
            _scannedFolders.value = current
            abookStorage.saveScannedFolderUri(path)
            _statusMessage.value = "Ordner hinzugefügt – scanne..."

            viewModelScope.launch {
                if (path.startsWith("content://")) {
                    val count = abookStorage.importFromFolderUri(Uri.parse(path))
                    _statusMessage.value = "$count Hörbuch(er) aus Ordner importiert"
                } else {
                    val imported = abookStorage.scanAndImport(listOf(path))
                    _statusMessage.value = "${imported.size} Hörbuch(er) aus Ordner importiert"
                }
            }
        }
    }


    fun removeScannedFolder(path: String) {
        val current = _scannedFolders.value.toMutableList()
        if (current.remove(path)) {
            _scannedFolders.value = current
            abookStorage.removeScannedFolderUri(path)
            _statusMessage.value = "Ordner entfernt"
        }
    }

    fun calculateCacheSize() {
        viewModelScope.launch {
            val cacheDir = context.cacheDir
            var totalSize = 0L
            cacheDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                }
            }
            _cacheSizeBytes.value = totalSize
        }
    }

    fun clearAppCache() {
        viewModelScope.launch {
            val cacheDir = context.cacheDir
            var deletedFiles = 0
            cacheDir.listFiles()?.forEach { file ->
                if (file.deleteRecursively()) {
                    deletedFiles++
                }
            }
            calculateCacheSize()
            _statusMessage.value = "App-Cache erfolgreich bereinigt"
        }
    }

    fun setSkipDuration(seconds: Int) {
        _skipDurationSeconds.value = seconds
    }

    fun setAutoRewindSeconds(seconds: Int) {
        _autoRewindSeconds.value = seconds
    }

    fun setAppThemeMode(themeMode: AppThemeMode) {
        _appThemeMode.value = themeMode
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
