package de.f_soft_studio.abookplayer

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import de.f_soft_studio.abookplayer.data.local.db.AbookDatabase
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.usecase.RecordListeningTimeUseCase
import de.f_soft_studio.abookplayer.domain.usecase.SaveProgressUseCase
import de.f_soft_studio.abookplayer.player.controller.PlaybackController
import de.f_soft_studio.abookplayer.storage.AbookStorage
import de.f_soft_studio.abookplayer.ui.AppRoot
import de.f_soft_studio.abookplayer.ui.theme.ABookTheme

/**
 * Haupt-Activity der ABook Player Anwendung.
 * Initialisiert Datenbank, Repository, AbookStorage und PlaybackController.
 * Fordert Speicher- und Benachrichtigungsrechte an.
 */
class MainActivity : ComponentActivity() {

    private lateinit var playbackController: PlaybackController

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestStorageAndNotificationPermissions()

        val db = AbookDatabase.getInstance(applicationContext)
        val repository = AudiobookRepository(
            audiobookDao = db.audiobookDao(),
            chapterDao = db.chapterDao(),
            bookmarkDao = db.bookmarkDao(),
            listeningSessionDao = db.listeningSessionDao()
        )
        val storage = AbookStorage(applicationContext, repository)
        val recordListeningTimeUseCase = RecordListeningTimeUseCase(repository)
        val saveProgressUseCase = SaveProgressUseCase(repository, recordListeningTimeUseCase)

        playbackController = PlaybackController.getInstance(applicationContext)

        setContent {
            ABookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(
                        repository = repository,
                        storage = storage,
                        playbackController = playbackController
                    )
                }
            }
        }
    }

    private fun requestStorageAndNotificationPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Do not release playbackController here if playback is actively running in background service
        if (::playbackController.isInitialized && !playbackController.isPlaying.value) {
            // Keep instance available for background widget interaction
        }
    }
}
