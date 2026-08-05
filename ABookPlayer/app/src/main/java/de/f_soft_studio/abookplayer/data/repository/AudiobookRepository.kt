package de.f_soft_studio.abookplayer.data.repository

import de.f_soft_studio.abookplayer.data.local.dao.AudiobookDao
import de.f_soft_studio.abookplayer.data.local.dao.BookmarkDao
import de.f_soft_studio.abookplayer.data.local.dao.ChapterDao
import de.f_soft_studio.abookplayer.data.local.dao.DailyListenSummary
import de.f_soft_studio.abookplayer.data.local.dao.ListeningSessionDao
import de.f_soft_studio.abookplayer.data.local.entity.ListeningSessionEntity
import de.f_soft_studio.abookplayer.data.local.entity.toDomainModel
import de.f_soft_studio.abookplayer.data.local.entity.toEntity
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Bookmark
import de.f_soft_studio.abookplayer.domain.model.Chapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository zur Kapselung des Datenzugriffs auf Hörbücher, Kapitel, Lesezeichen und Hörstatistiken.
 */
class AudiobookRepository(
    private val audiobookDao: AudiobookDao,
    private val chapterDao: ChapterDao,
    private val bookmarkDao: BookmarkDao,
    private val listeningSessionDao: ListeningSessionDao? = null
) {

    /**
     * Liefert alle Hörbücher als Flow sortiert nach dem letzten Wiedergabe-Zeitstempel.
     */
    fun getAllAudiobooks(): Flow<List<Audiobook>> {
        return audiobookDao.getAllAudiobooksFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Ruft ein spezifisches Hörbuch anhand seiner ID ab.
     */
    suspend fun getAudiobookById(id: Long): Audiobook? {
        return audiobookDao.getAudiobookById(id)?.toDomainModel()
    }

    /**
     * Fügt ein neues Hörbuch ein oder aktualisiert ein bestehendes.
     */
    suspend fun saveAudiobook(audiobook: Audiobook): Long {
        return audiobookDao.insertAudiobook(audiobook.toEntity())
    }

    /**
     * Aktualisiert den Wiedergabefortschritt eines Hörbuchs.
     */
    suspend fun updateProgress(id: Long, position: Long) {
        audiobookDao.updateProgress(id, position)
    }

    /**
     * Aktualisiert Cover-URI und Beschreibung eines Hörbuchs.
     */
    suspend fun updateCoverAndDescription(id: Long, coverUri: String?, description: String?) {
        audiobookDao.updateCoverAndDescription(id, coverUri, description)
    }

    /**
     * Aktualisiert Serien-Informationen eines Hörbuchs.
     */
    suspend fun updateSeriesInfo(id: Long, series: String?, seriesOrder: Int?) {
        audiobookDao.updateSeriesInfo(id, series, seriesOrder)
    }

    /**
     * Löscht ein Hörbuch anhand seiner ID.
     */
    suspend fun deleteAudiobook(id: Long) {
        audiobookDao.deleteAudiobookById(id)
    }

    /**
     * Liefert die Kapitelliste für ein Hörbuch.
     */
    fun getChaptersForAudiobook(audiobookId: Long): Flow<List<Chapter>> {
        return chapterDao.getChaptersForAudiobookFlow(audiobookId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Speichert Kapitel für ein Hörbuch.
     */
    suspend fun saveChapters(chapters: List<Chapter>) {
        chapterDao.insertChapters(chapters.map { it.toEntity() })
    }

    /**
     * Liefert alle Lesezeichen für ein Hörbuch.
     */
    fun getBookmarksForAudiobook(audiobookId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksForAudiobookFlow(audiobookId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Speichert ein neues Lesezeichen.
     */
    suspend fun addBookmark(bookmark: Bookmark): Long {
        return bookmarkDao.insertBookmark(bookmark.toEntity())
    }

    /**
     * Aktualisiert ein bestehendes Lesezeichen.
     */
    suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.insertBookmark(bookmark.toEntity())
    }

    /**
     * Löscht ein Lesezeichen.
     */
    suspend fun deleteBookmark(id: Long) {
        bookmarkDao.deleteBookmarkById(id)
    }

    // --- Hörstatistik-Methoden ---

    /**
     * Zeichnet eine Hörsitzung mit der angegebenen Dauer in Sekunden auf.
     */
    suspend fun recordListeningSession(
        audiobookId: Long,
        durationSeconds: Long,
        date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    ) {
        if (durationSeconds <= 0) return
        listeningSessionDao?.insertSession(
            ListeningSessionEntity(
                audiobookId = audiobookId,
                date = date,
                durationSeconds = durationSeconds
            )
        )
    }

    /**
     * Gesamte Hörzeit in Sekunden über alle Bücher hinweg.
     */
    fun getTotalListenTimeSeconds(): Flow<Long> {
        return listeningSessionDao?.getTotalListenTimeSecondsFlow()?.map { it ?: 0L } ?: flowOf(0L)
    }

    /**
     * Hörzeit in Sekunden an einem bestimmten Tag (`yyyy-MM-dd`).
     */
    fun getListenTimeForDate(date: String): Flow<Long> {
        return listeningSessionDao?.getListenTimeForDateFlow(date)?.map { it ?: 0L } ?: flowOf(0L)
    }

    /**
     * Hörzeit in Sekunden in einem Datumsbereich.
     */
    fun getListenTimeForDateRange(startDate: String, endDate: String): Flow<Long> {
        return listeningSessionDao?.getListenTimeForDateRangeFlow(startDate, endDate)?.map { it ?: 0L } ?: flowOf(0L)
    }

    /**
     * Tägliche Hörzeiten ab dem Startdatum.
     */
    fun getDailySummaries(startDate: String): Flow<List<DailyListenSummary>> {
        return listeningSessionDao?.getDailySummariesFlow(startDate) ?: flowOf(emptyList())
    }

    /**
     * Alle Daten (`yyyy-MM-dd`), an denen aktiv gehört wurde.
     */
    fun getActiveListeningDates(): Flow<List<String>> {
        return listeningSessionDao?.getActiveListeningDatesFlow() ?: flowOf(emptyList())
    }
}
