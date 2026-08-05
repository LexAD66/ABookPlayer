package de.f_soft_studio.abookplayer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.f_soft_studio.abookplayer.domain.model.Bookmark

/**
 * Room-Entity zur Speicherung von Lesezeichen (Bookmarks) eines Hörbuchs.
 */
@Entity(
    tableName = "bookmarks",
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
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audiobookId: Long,
    val position: Long,
    val note: String,
    val createdAt: Long
)

/**
 * Konvertiert eine BookmarkEntity in das Domain-Modell [Bookmark].
 */
fun BookmarkEntity.toDomainModel(): Bookmark {
    return Bookmark(
        id = id,
        audiobookId = audiobookId,
        position = position,
        note = note,
        createdAt = createdAt
    )
}

/**
 * Konvertiert ein Bookmark Domain-Modell in die Entity [BookmarkEntity].
 */
fun Bookmark.toEntity(): BookmarkEntity {
    return BookmarkEntity(
        id = id,
        audiobookId = audiobookId,
        position = position,
        note = note,
        createdAt = createdAt
    )
}
