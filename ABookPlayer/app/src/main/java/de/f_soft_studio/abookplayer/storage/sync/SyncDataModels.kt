package de.f_soft_studio.abookplayer.storage.sync

import kotlinx.serialization.Serializable

/**
 * DTO für den Hörfortschritt eines einzelnen Hörbuchs beim Cloud-Sync.
 */
@Serializable
data class SyncAudiobookProgressDto(
    val id: Long = 0,
    val title: String,
    val author: String = "",
    val currentPosition: Long,
    val duration: Long = 0,
    val lastPlayed: Long,
    val isFinished: Boolean = false,
    val isFavorite: Boolean = false,
    val seriesName: String = "",
    val seriesOrder: Int = 0
)

/**
 * DTO für ein Lesezeichen beim Cloud-Sync.
 */
@Serializable
data class SyncBookmarkDto(
    val id: Long = 0,
    val audiobookTitle: String = "",
    val position: Long,
    val title: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * DTO für eine Hör-Session (Statistik) beim Cloud-Sync.
 */
@Serializable
data class SyncListeningSessionDto(
    val id: Long = 0,
    val timestamp: Long,
    val durationSeconds: Long
)

/**
 * Gesamt-Zustandsobjekt für den WebDAV-Export/Import (abook_sync_state.json).
 */
@Serializable
data class SyncStateDto(
    val version: Int = 1,
    val deviceId: String = "",
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val audiobooks: List<SyncAudiobookProgressDto> = emptyList(),
    val bookmarks: List<SyncBookmarkDto> = emptyList(),
    val listeningSessions: List<SyncListeningSessionDto> = emptyList()
)
