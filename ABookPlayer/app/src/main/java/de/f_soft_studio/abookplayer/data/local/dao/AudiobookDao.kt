package de.f_soft_studio.abookplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.f_soft_studio.abookplayer.data.local.entity.AudiobookEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) für Datenbankoperationen bezüglich Hörbüchern.
 */
@Dao
interface AudiobookDao {

    @Query("SELECT * FROM audiobooks ORDER BY lastPlayed DESC")
    fun getAllAudiobooksFlow(): Flow<List<AudiobookEntity>>

    @Query("SELECT * FROM audiobooks WHERE id = :id LIMIT 1")
    suspend fun getAudiobookById(id: Long): AudiobookEntity?

    @Query("SELECT * FROM audiobooks WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY lastPlayed DESC")
    suspend fun searchAudiobooks(query: String): List<AudiobookEntity>

    @Query("SELECT * FROM audiobooks WHERE id = :id LIMIT 1")
    fun getAudiobookByIdFlow(id: Long): Flow<AudiobookEntity?>

    /**
     * Fügt ein neues Hörbuch ein.
     *
     * ACHTUNG: Bei existierender ID triggert REPLACE ein SQLite-Delete und damit
     * die Fremdschlüssel-Kaskade (CASCADE DELETE) auf [ChapterEntity]!
     * Für existierende Hörbücher MUSS [updateAudiobook] verwendet werden.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudiobook(audiobook: AudiobookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudiobooks(audiobooks: List<AudiobookEntity>)

    /**
     * Aktualisiert ein bestehendes Hörbuch gezielt ohne Kaskaden-Effekt auf Kind-Tabellen.
     */
    @Update
    suspend fun updateAudiobook(audiobook: AudiobookEntity)

    @Query("UPDATE audiobooks SET currentPosition = :position, lastPlayed = :lastPlayed WHERE id = :id")
    suspend fun updateProgress(id: Long, position: Long, lastPlayed: Long = System.currentTimeMillis())

    @Query("UPDATE audiobooks SET coverUri = :coverUri, description = COALESCE(:description, description) WHERE id = :id")
    suspend fun updateCoverAndDescription(id: Long, coverUri: String?, description: String?)

    @Query("UPDATE audiobooks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE audiobooks SET parentSeries = :parentSeries, series = :series, seriesOrder = :seriesOrder WHERE id = :id")
    suspend fun updateSeriesAndParentSeriesInfo(id: Long, parentSeries: String?, series: String?, seriesOrder: Int?)

    @Query("UPDATE audiobooks SET series = :series, seriesOrder = :seriesOrder WHERE id = :id")
    suspend fun updateSeriesInfo(id: Long, series: String?, seriesOrder: Int?)


    @Delete
    suspend fun deleteAudiobook(audiobook: AudiobookEntity)

    @Query("DELETE FROM audiobooks WHERE id = :id")
    suspend fun deleteAudiobookById(id: Long)
}
