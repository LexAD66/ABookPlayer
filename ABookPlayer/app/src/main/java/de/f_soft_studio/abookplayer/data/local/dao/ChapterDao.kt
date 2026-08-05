package de.f_soft_studio.abookplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.f_soft_studio.abookplayer.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) für Kapiteloperationen.
 */
@Dao
interface ChapterDao {

    @Query("SELECT * FROM chapters WHERE audiobookId = :audiobookId ORDER BY startTime ASC")
    fun getChaptersForAudiobookFlow(audiobookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE audiobookId = :audiobookId ORDER BY startTime ASC")
    suspend fun getChaptersForAudiobook(audiobookId: Long): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE audiobookId = :audiobookId")
    suspend fun deleteChaptersForAudiobook(audiobookId: Long)
}
