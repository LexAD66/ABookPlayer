package de.f_soft_studio.abookplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.f_soft_studio.abookplayer.data.local.entity.ListeningSessionEntity
import kotlinx.coroutines.flow.Flow

data class DailyListenSummary(
    val date: String,
    val totalSeconds: Long
)

/**
 * Data Access Object (DAO) für Hörsitzungen / Statistiken.
 */
@Dao
interface ListeningSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ListeningSessionEntity): Long

    @Query("SELECT SUM(durationSeconds) FROM listening_sessions")
    fun getTotalListenTimeSecondsFlow(): Flow<Long?>

    @Query("SELECT SUM(durationSeconds) FROM listening_sessions WHERE date = :date")
    fun getListenTimeForDateFlow(date: String): Flow<Long?>

    @Query("SELECT SUM(durationSeconds) FROM listening_sessions WHERE date >= :startDate AND date <= :endDate")
    fun getListenTimeForDateRangeFlow(startDate: String, endDate: String): Flow<Long?>

    @Query("SELECT date, SUM(durationSeconds) AS totalSeconds FROM listening_sessions WHERE date >= :startDate GROUP BY date ORDER BY date ASC")
    fun getDailySummariesFlow(startDate: String): Flow<List<DailyListenSummary>>

    @Query("SELECT DISTINCT date FROM listening_sessions WHERE durationSeconds > 0 ORDER BY date DESC")
    fun getActiveListeningDatesFlow(): Flow<List<String>>
}
