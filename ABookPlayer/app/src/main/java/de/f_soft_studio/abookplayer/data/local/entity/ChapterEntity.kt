package de.f_soft_studio.abookplayer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.f_soft_studio.abookplayer.domain.model.Chapter

/**
 * Room-Entity zur Speicherung von Kapiteln eines Hörbuchs.
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = AudiobookEntity::class,
            parentColumns = ["id"],
            childColumns = ["audiobookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["audiobookId"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audiobookId: Long,
    val title: String,
    val startTime: Long,
    val audioPath: String? = null
)

/**
 * Konvertiert eine ChapterEntity in das Domain-Modell [Chapter].
 */
fun ChapterEntity.toDomainModel(): Chapter {
    return Chapter(
        id = id,
        audiobookId = audiobookId,
        title = title,
        startTime = startTime,
        audioPath = audioPath
    )
}

/**
 * Konvertiert ein Chapter Domain-Modell in die Entity [ChapterEntity].
 */
fun Chapter.toEntity(): ChapterEntity {
    return ChapterEntity(
        id = id,
        audiobookId = audiobookId,
        title = title,
        startTime = startTime,
        audioPath = audioPath
    )
}
