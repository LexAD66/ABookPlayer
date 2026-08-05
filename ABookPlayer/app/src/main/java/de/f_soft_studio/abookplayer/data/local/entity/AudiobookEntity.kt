package de.f_soft_studio.abookplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.f_soft_studio.abookplayer.domain.model.Audiobook

/**
 * Room-Entity zur Speicherung von Hörbuch-Stammdaten in der SQLite-Datenbank.
 */
@Entity(tableName = "audiobooks")
data class AudiobookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val narrator: String? = null,
    val filePath: String,
    val coverUri: String?,
    val description: String? = null,
    val duration: Long,
    val currentPosition: Long,
    val lastPlayed: Long,
    val series: String? = null,
    val seriesOrder: Int? = null
)

/**
 * Konvertiert eine Room-Entity in ein Domain-Modell [Audiobook].
 */
fun AudiobookEntity.toDomainModel(): Audiobook {
    return Audiobook(
        id = id,
        title = title,
        author = author,
        narrator = narrator,
        filePath = filePath,
        coverUri = coverUri,
        description = description,
        duration = duration,
        currentPosition = currentPosition,
        lastPlayed = lastPlayed,
        series = series,
        seriesOrder = seriesOrder
    )
}

/**
 * Konvertiert ein Domain-Modell [Audiobook] in eine Room-Entity [AudiobookEntity].
 */
fun Audiobook.toEntity(): AudiobookEntity {
    return AudiobookEntity(
        id = id,
        title = title,
        author = author,
        narrator = narrator,
        filePath = filePath,
        coverUri = coverUri,
        description = description,
        duration = duration,
        currentPosition = currentPosition,
        lastPlayed = lastPlayed,
        series = series,
        seriesOrder = seriesOrder
    )
}
