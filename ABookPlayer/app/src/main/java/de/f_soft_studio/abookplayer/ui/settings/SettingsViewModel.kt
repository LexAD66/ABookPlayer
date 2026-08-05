package de.f_soft_studio.abookplayer.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

import de.f_soft_studio.abookplayer.ui.theme.AppThemeMode

/**
 * ViewModel für den Einstellungsbildschirm.
 * Verwaltet App-Cache-Größe, Sprungweiten und Einstellungen.
 */
class SettingsViewModel(
    private val context: Context
) : ViewModel() {

    private val _cacheSizeBytes = MutableStateFlow(0L)
    val cacheSizeBytes: StateFlow<Long> = _cacheSizeBytes.asStateFlow()

    private val _skipDurationSeconds = MutableStateFlow(10)
    val skipDurationSeconds: StateFlow<Int> = _skipDurationSeconds.asStateFlow()

    private val _autoRewindSeconds = MutableStateFlow(10)
    val autoRewindSeconds: StateFlow<Int> = _autoRewindSeconds.asStateFlow()

    private val _appThemeMode = MutableStateFlow(AppThemeMode.DARK_STAGE)
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    private val _scannedFolders = MutableStateFlow<List<String>>(
        listOf("/storage/emulated/0/Download/ABookPlayer", "/storage/emulated/0/Audiobooks")
    )
    val scannedFolders: StateFlow<List<String>> = _scannedFolders.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun addScannedFolder(path: String) {
        val current = _scannedFolders.value.toMutableList()
        if (!current.contains(path)) {
            current.add(path)
            _scannedFolders.value = current
            _statusMessage.value = "Ordner hinzugefügt"
        }
    }

    fun removeScannedFolder(path: String) {
        val current = _scannedFolders.value.toMutableList()
        if (current.remove(path)) {
            _scannedFolders.value = current
            _statusMessage.value = "Ordner entfernt"
        }
    }

    init {
        calculateCacheSize()
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
