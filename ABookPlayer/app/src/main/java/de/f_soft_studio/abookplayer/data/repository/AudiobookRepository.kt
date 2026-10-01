package de.f_soft_studio.abookplayer.data.repository

import de.f_soft_studio.abookplayer.data.local.dao.AudiobookDao
import de.f_soft_studio.abookplayer.data.local.dao.BookmarkDao
import de.f_soft_studio.abookplayer.data.local.dao.ChapterDao
import de.f_soft_studio.abookplayer.data.local.dao.CharacterDao
import de.f_soft_studio.abookplayer.data.local.dao.DailyListenSummary
import de.f_soft_studio.abookplayer.data.local.dao.ListeningSessionDao
import de.f_soft_studio.abookplayer.data.local.entity.ListeningSessionEntity
import de.f_soft_studio.abookplayer.data.local.entity.toDomainModel
import de.f_soft_studio.abookplayer.data.local.entity.toEntity
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.BookCharacter
import de.f_soft_studio.abookplayer.domain.model.Bookmark
import de.f_soft_studio.abookplayer.domain.model.Chapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository zur Kapselung des Datenzugriffs auf Hörbücher, Kapitel, Lesezeichen, Hörstatistiken und Buchfiguren.
 */
class AudiobookRepository(
    private val audiobookDao: AudiobookDao,
    private val chapterDao: ChapterDao,
    private val bookmarkDao: BookmarkDao,
    private val listeningSessionDao: ListeningSessionDao? = null,
    private val characterDao: CharacterDao? = null
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
     * Sucht nach Hörbüchern anhand von Titel oder Autor.
     */
    suspend fun searchAudiobooks(query: String): List<Audiobook> {
        return audiobookDao.searchAudiobooks(query).map { it.toDomainModel() }
    }

    /**
     * Fügt ein neues Hörbuch ein oder aktualisiert ein bestehendes.
     *
     * WICHTIG ZUR DATENINTEGRITÄT:
     * Wenn `audiobook.id > 0` ist, MUSS zwingend [AudiobookDao.updateAudiobook] verwendet
     * werden, da ein Aufruf von `insertAudiobook` mit `OnConflictStrategy.REPLACE`
     * intern ein SQLite-Delete der Zeile bewirkt, was die Fremdschlüssel-Kaskade
     * (`onDelete = CASCADE`) auf der Tabelle `chapters` auslöst und alle Kapitel löschen würde!
     */
    suspend fun saveAudiobook(audiobook: Audiobook): Long {
        return if (audiobook.id > 0L && audiobookDao.getAudiobookById(audiobook.id) != null) {
            audiobookDao.updateAudiobook(audiobook.toEntity())
            audiobook.id
        } else {
            audiobookDao.insertAudiobook(audiobook.toEntity())
        }
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
     * Aktualisiert Serien-Informationen (inkl. übergeordneter Reihe) eines Hörbuchs.
     */
    suspend fun updateSeriesInfo(id: Long, parentSeries: String?, series: String?, seriesOrder: Int?) {
        audiobookDao.updateSeriesAndParentSeriesInfo(id, parentSeries, series, seriesOrder)
    }

    /**
     * Aktualisiert Serien-Informationen eines Hörbuchs (Legacy-Kompatibilität).
     */
    suspend fun updateSeriesInfo(id: Long, series: String?, seriesOrder: Int?) {
        audiobookDao.updateSeriesInfo(id, series, seriesOrder)
    }

    /**
     * Setzt oder entfernt den Favoritenstatus eines Hörbuchs.
     */
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        audiobookDao.updateFavorite(id, isFavorite)
    }

    /**
     * Löscht ein Hörbuch anhand seiner ID.
     */
    suspend fun deleteAudiobook(id: Long) {
        audiobookDao.deleteAudiobookById(id)
    }

    /**
     * Findet und entfernt doppelte Einträge (gleicher Titel & Autor) sowie verwaiste Datenbankeinträge,
     * deren Dateien auf dem Speicher gelöscht wurden, und repariert 0-min-Hörbücher.
     */
    suspend fun cleanupDuplicatesAndOrphans(context: android.content.Context? = null): LibraryCleanupResult {
        val allEntities = audiobookDao.getAllAudiobooksFlow().first()
        var duplicatesCount = 0
        var orphansCount = 0
        val idsToDelete = mutableSetOf<Long>()

        val pathGroups = allEntities.groupBy { it.filePath }
        for ((_, group) in pathGroups) {
            if (group.size > 1) {
                val sorted = group.sortedWith(
                    compareByDescending<de.f_soft_studio.abookplayer.data.local.entity.AudiobookEntity> { it.currentPosition > 0 }
                        .thenByDescending { it.lastPlayed }
                        .thenBy { it.id }
                )
                val duplicates = sorted.drop(1)
                duplicates.forEach { dup ->
                    idsToDelete.add(dup.id)
                    duplicatesCount++
                }
            }
        }

        val remainingEntities = allEntities.filterNot { idsToDelete.contains(it.id) }
        val titleAuthorGroups = remainingEntities.groupBy {
            "${it.title.trim().lowercase()}_${it.author.trim().lowercase()}"
        }
        for ((_, group) in titleAuthorGroups) {
            if (group.size > 1) {
                val sorted = group.sortedWith(
                    compareByDescending<de.f_soft_studio.abookplayer.data.local.entity.AudiobookEntity> { it.currentPosition > 0 }
                        .thenByDescending { it.lastPlayed }
                        .thenBy { it.id }
                )
                val duplicates = sorted.drop(1)
                duplicates.forEach { dup ->
                    idsToDelete.add(dup.id)
                    duplicatesCount++
                }
            }
        }

        val nonDeletedEntities = allEntities.filterNot { idsToDelete.contains(it.id) }
        for (book in nonDeletedEntities) {
            val path = book.filePath
            if (path.isNotBlank()) {
                if (path.startsWith("content://")) {
                    if (context != null) {
                        try {
                            val uri = android.net.Uri.parse(path)
                            val hasPermission = context.contentResolver.persistedUriPermissions.any {
                                it.uri == uri || uri.toString().startsWith(it.uri.toString())
                            }
                            if (!hasPermission) {
                                val doc = androidx.documentfile.provider.DocumentFile.fromSingleUri(context, uri)
                                    ?: androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)
                                if (doc == null || !doc.exists()) {
                                    idsToDelete.add(book.id)
                                    orphansCount++
                                }
                            }
                        } catch (e: Exception) {
                            // Ignorieren falls URI-Zugriff temporär nicht möglich
                        }
                    }
                } else {
                    val file = java.io.File(path)
                    // Verwaist ist ein Buch auch dann, wenn sein Pfad ein Verzeichnis ist,
                    // das keine Audiodateien (mehr) enthält – z. B. ein leerer imported_*-Ordner,
                    // dessen Dateien verschoben/gelöscht wurden. `File.exists()` allein reicht
                    // nicht, weil es für Verzeichnisse `true` liefert.
                    val isOrphan = when {
                        !file.exists() -> true
                        file.isDirectory -> file.walkTopDown().maxDepth(5)
                            .none { it.isFile && de.f_soft_studio.abookplayer.storage.FolderScanner.isAudioFile(it) }
                        else -> false
                    }
                    if (isOrphan) {
                        idsToDelete.add(book.id)
                        orphansCount++
                    }
                }
            }
        }


        for (id in idsToDelete) {
            audiobookDao.deleteAudiobookById(id)
            chapterDao.deleteChaptersForAudiobook(id)
        }

        // Leere imported_* Verzeichnisse auf dem Speicher bereinigen
        if (context != null) {
            try {
                val libDir = de.f_soft_studio.abookplayer.storage.LibraryLocationManager.getLibraryDir(context)
                libDir.listFiles()?.forEach { f ->
                    if (f.isDirectory && f.name.startsWith("imported_")) {
                        val hasAudio = f.walkTopDown().any { it.isFile && de.f_soft_studio.abookplayer.storage.FolderScanner.isAudioFile(it) }
                        if (!hasAudio) {
                            f.deleteRecursively()
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        val zeroDurationFixed = recalculateZeroDurationBooks(context)

        return LibraryCleanupResult(
            duplicatesRemoved = duplicatesCount,
            orphansRemoved = orphansCount,
            zeroDurationFixed = zeroDurationFixed
        )
    }

    /**
     * Sucht alle Hörbücher mit 0 min Gesamtlänge, berechnet deren Laufzeit neu und aktualisiert die Datenbank.
     */
    suspend fun recalculateZeroDurationBooks(context: android.content.Context? = null): Int {
        val allBooks = audiobookDao.getAllAudiobooksFlow().first()
        val zeroDurationBooks = allBooks.filter { it.duration <= 0L }
        var fixedCount = 0

        for (bookEntity in zeroDurationBooks) {
            val bookId = bookEntity.id
            val chapterEntities = chapterDao.getChaptersForAudiobookFlow(bookId).first()
            var calculatedDuration = 0L

            if (chapterEntities.isNotEmpty()) {
                for (ch in chapterEntities) {
                    val path = ch.audioPath
                    if (!path.isNullOrBlank()) {
                        val dur = de.f_soft_studio.abookplayer.util.ChapterDurations.readDurationMs(context, path)
                        if (dur > 0L) {
                            calculatedDuration += dur
                        }
                    }
                }
            }

            if (calculatedDuration <= 0L && bookEntity.filePath.isNotBlank()) {
                val path = bookEntity.filePath
                if (path.startsWith("content://")) {
                    calculatedDuration = de.f_soft_studio.abookplayer.util.ChapterDurations.readDurationMs(context, path)
                } else {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        if (file.isFile) {
                            calculatedDuration = de.f_soft_studio.abookplayer.util.ChapterDurations.readDurationMs(context, path)
                        } else if (file.isDirectory) {
                            val audioFiles = file.walkTopDown()
                                .filter { it.isFile && de.f_soft_studio.abookplayer.storage.FolderScanner.isAudioFile(it) }
                                .toList()
                            calculatedDuration = audioFiles.sumOf { de.f_soft_studio.abookplayer.util.ChapterDurations.readDurationMs(it) }
                        }
                    }
                }
            }

            if (calculatedDuration > 0L) {
                saveAudiobook(bookEntity.toDomainModel().copy(duration = calculatedDuration))
                fixedCount++
            }
        }
        return fixedCount
    }

data class LibraryCleanupResult(
    val duplicatesRemoved: Int,
    val orphansRemoved: Int,
    val zeroDurationFixed: Int = 0
)

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
        if (chapters.isEmpty()) return
        val bookId = chapters.first().audiobookId
        if (bookId > 0L) {
            chapterDao.deleteChaptersForAudiobook(bookId)
        }
        chapterDao.insertChapters(chapters.map { it.toEntity().copy(id = 0L) })
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

    // --- Buchfiguren / Personenregister (Characters) ---

    /**
     * Liefert alle Buchfiguren für ein Hörbuch.
     */
    fun getCharactersForAudiobook(audiobookId: Long): Flow<List<BookCharacter>> {
        return characterDao?.getCharactersForAudiobookFlow(audiobookId)?.map { entities ->
            entities.map { it.toDomainModel() }
        } ?: flowOf(emptyList())
    }

    /**
     * Speichert eine Buchfigur (neu anlegen oder aktualisieren).
     */
    suspend fun saveCharacter(character: BookCharacter): Long {
        return characterDao?.insertCharacter(character.toEntity()) ?: 0L
    }

    /**
     * Löscht eine Buchfigur anhand ihrer ID.
     */
    suspend fun deleteCharacter(id: Long) {
        characterDao?.deleteCharacterById(id)
    }
}

