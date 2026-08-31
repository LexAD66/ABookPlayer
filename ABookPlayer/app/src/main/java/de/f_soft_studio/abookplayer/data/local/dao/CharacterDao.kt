package de.f_soft_studio.abookplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.f_soft_studio.abookplayer.data.local.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) für Buchfiguren / Charaktere (Personenregister).
 */
@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters WHERE audiobookId = :audiobookId ORDER BY isPrimary DESC, name ASC")
    fun getCharactersForAudiobookFlow(audiobookId: Long): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacterById(id: Long)
}
