package de.f_soft_studio.abookplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-Entity zur Speicherung von Hörsitzungen (Hörstatistiken) in der SQLite-Datenbank.
 */
@Entity(tableName = "listening_sessions")
data class ListeningSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audiobookId: Long,
    val date: String, // YYYY-MM-DD
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)
