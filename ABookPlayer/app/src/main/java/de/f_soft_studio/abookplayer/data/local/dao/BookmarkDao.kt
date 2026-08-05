package de.f_soft_studio.abookplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.f_soft_studio.abookplayer.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) für Lesezeichen.
 */
@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks WHERE audiobookId = :audiobookId ORDER BY position ASC")
    fun getBookmarksForAudiobookFlow(audiobookId: Long): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)
}
