package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Bookmark
import de.f_soft_studio.abookplayer.storage.sync.SyncAudiobookProgressDto
import de.f_soft_studio.abookplayer.storage.sync.SyncBookmarkDto
import de.f_soft_studio.abookplayer.storage.sync.SyncListeningSessionDto
import de.f_soft_studio.abookplayer.storage.sync.SyncStateDto
import de.f_soft_studio.abookplayer.storage.sync.WebDavSyncManager
import kotlinx.coroutines.flow.firstOrNull

/**
 * Ergebnis eines Cloud-Synchronisationslaufs.
 */
data class SyncResult(
    val isSuccess: Boolean,
    val updatedAudiobooksCount: Int = 0,
    val addedBookmarksCount: Int = 0,
    val syncedSessionsCount: Int = 0,
    val message: String = ""
)

/**
 * SyncProgressUseCase: Führt die bidirektionale Synchronisation des Hörfortschritts,
 * der Lesezeichen und der Hörstatistiken mit dem WebDAV-Cloud-Server durch.
 */
class SyncProgressUseCase(
    private val repository: AudiobookRepository,
    private val webDavSyncManager: WebDavSyncManager
) {

    suspend operator fun invoke(): SyncResult {
        if (!webDavSyncManager.isConfigured) {
            return SyncResult(isSuccess = false, message = "WebDAV-Zugangsdaten nicht konfiguriert.")
        }

        // 1. Lokale Daten abrufen
        val localAudiobooks = repository.getAllAudiobooks().firstOrNull() ?: emptyList()
        val allLocalBookmarks = mutableListOf<Bookmark>()
        localAudiobooks.forEach { book ->
            val bms = repository.getBookmarksForAudiobook(book.id).firstOrNull() ?: emptyList()
            allLocalBookmarks.addAll(bms)
        }

        // 2. Remote Zustand herunterladen
        val downloadResult = webDavSyncManager.downloadSyncState()
        if (downloadResult.isFailure) {
            return SyncResult(
                isSuccess = false,
                message = downloadResult.exceptionOrNull()?.message ?: "Download vom WebDAV-Server fehlgeschlagen."
            )
        }

        val remoteState = downloadResult.getOrNull()
        var updatedAudiobooksCount = 0
        var addedBookmarksCount = 0

        // Map zur schnellen Zuordnung von Titeln
        val localBooksMap = localAudiobooks.associateBy { sanitizeTitle(it.title, it.author) }

        if (remoteState != null) {
            // 3. Merging Hörfortschritte
            remoteState.audiobooks.forEach { remoteBook ->
                val key = sanitizeTitle(remoteBook.title, remoteBook.author)
                val localBook = localBooksMap[key]

                if (localBook != null) {
                    // Falls Remote neuer ist als Lokal, aktualisieren wir lokal
                    if (remoteBook.lastPlayed > localBook.lastPlayed) {
                        val updated = localBook.copy(
                            currentPosition = remoteBook.currentPosition,
                            series = remoteBook.seriesName.ifBlank { localBook.series },
                            seriesOrder = if (remoteBook.seriesOrder > 0) remoteBook.seriesOrder else localBook.seriesOrder,
                            lastPlayed = remoteBook.lastPlayed
                        )
                        repository.saveAudiobook(updated)
                        updatedAudiobooksCount++
                    }
                }
            }

            // 4. Merging Bookmarks
            val localBookmarkKeys = allLocalBookmarks.map { "${it.audiobookId}_${it.position}" }.toSet()
            remoteState.bookmarks.forEach { remoteBm ->
                val matchingLocalBook = localAudiobooks.firstOrNull { sanitizeTitle(it.title, it.author) == sanitizeTitle(remoteBm.audiobookTitle, "") }
                if (matchingLocalBook != null) {
                    val bmKey = "${matchingLocalBook.id}_${remoteBm.position}"
                    if (bmKey !in localBookmarkKeys) {
                        val newBookmark = Bookmark(
                            audiobookId = matchingLocalBook.id,
                            position = remoteBm.position,
                            note = remoteBm.note.ifBlank { remoteBm.title },
                            createdAt = remoteBm.createdAt
                        )
                        repository.addBookmark(newBookmark)
                        addedBookmarksCount++
                    }
                }
            }
        }

        // 5. Neuen zusammengeführten Gesamt-Zustand für den Upload aufbauen
        val latestLocalAudiobooks = repository.getAllAudiobooks().firstOrNull() ?: emptyList()
        val latestLocalBookmarks = mutableListOf<Bookmark>()
        latestLocalAudiobooks.forEach { book ->
            val bms = repository.getBookmarksForAudiobook(book.id).firstOrNull() ?: emptyList()
            latestLocalBookmarks.addAll(bms)
        }

        val syncAudiobooksDto = latestLocalAudiobooks.map { book ->
            SyncAudiobookProgressDto(
                id = book.id,
                title = book.title,
                author = book.author,
                currentPosition = book.currentPosition,
                duration = book.duration,
                lastPlayed = book.lastPlayed,
                seriesName = book.series.orEmpty(),
                seriesOrder = book.seriesOrder ?: 0
            )
        }

        val syncBookmarksDto = latestLocalBookmarks.map { bm ->
            val book = latestLocalAudiobooks.firstOrNull { it.id == bm.audiobookId }
            SyncBookmarkDto(
                id = bm.id,
                audiobookTitle = book?.title.orEmpty(),
                position = bm.position,
                title = bm.note,
                note = bm.note,
                createdAt = bm.createdAt
            )
        }

        val updatedState = SyncStateDto(
            version = 1,
            deviceId = webDavSyncManager.deviceId,
            lastSyncedAt = System.currentTimeMillis(),
            audiobooks = syncAudiobooksDto,
            bookmarks = syncBookmarksDto,
            listeningSessions = emptyList()
        )

        // 6. Upload des aktualisierten Zustands
        val uploadResult = webDavSyncManager.uploadSyncState(updatedState)
        if (uploadResult.isFailure) {
            return SyncResult(
                isSuccess = false,
                message = uploadResult.exceptionOrNull()?.message ?: "Upload des Sync-Zustands fehlgeschlagen."
            )
        }

        return SyncResult(
            isSuccess = true,
            updatedAudiobooksCount = updatedAudiobooksCount,
            addedBookmarksCount = addedBookmarksCount,
            message = "Erfolgreich synchronisiert! ($updatedAudiobooksCount Hörbücher aktualisiert)."
        )
    }

    private fun sanitizeTitle(title: String, author: String): String {
        return "${title.trim().lowercase()}_${author.trim().lowercase()}"
    }
}
