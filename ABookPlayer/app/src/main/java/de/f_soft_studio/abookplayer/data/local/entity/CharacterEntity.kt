package de.f_soft_studio.abookplayer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

import de.f_soft_studio.abookplayer.domain.model.BookCharacter

/**
 * Room-Entity zur Speicherung von Buchfiguren/Charakteren (Personenregister) eines Hörbuchs.
 */
@Entity(
    tableName = "characters",
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
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audiobookId: Long,
    val name: String,
    val role: String = "",
    val description: String = "",
    val relationship: String = "",
    val isPrimary: Boolean = false
)

fun CharacterEntity.toDomainModel(): BookCharacter {
    return BookCharacter(
        id = id,
        audiobookId = audiobookId,
        name = name,
        role = role,
        description = description,
        relationship = relationship,
        isPrimary = isPrimary
    )
}

fun BookCharacter.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = id,
        audiobookId = audiobookId,
        name = name,
        role = role,
        description = description,
        relationship = relationship,
        isPrimary = isPrimary
    )
}

