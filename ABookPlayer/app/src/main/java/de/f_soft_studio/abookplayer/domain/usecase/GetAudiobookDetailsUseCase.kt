package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Bookmark
import de.f_soft_studio.abookplayer.domain.model.Chapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Kombiniertes Datenmodell für die Hörbuch-Detailansicht.
 */
data class AudiobookDetails(
    val audiobook: Audiobook,
    val chapters: List<Chapter>,
    val bookmarks: List<Bookmark>
)

/**
 * UseCase zum Abrufen aller Details eines Hörbuchs (Stammdaten, Kapitel, Lesezeichen) als kombinierter Flow.
 */
class GetAudiobookDetailsUseCase(
    private val repository: AudiobookRepository
) {
    fun getDetailsFlow(audiobookId: Long): Flow<AudiobookDetails?> {
        val chaptersFlow = repository.getChaptersForAudiobook(audiobookId)
        val bookmarksFlow = repository.getBookmarksForAudiobook(audiobookId)

        return combine(chaptersFlow, bookmarksFlow) { chapters, bookmarks ->
            val book = repository.getAudiobookById(audiobookId) ?: return@combine null
            AudiobookDetails(
                audiobook = book,
                chapters = chapters,
                bookmarks = bookmarks
            )
        }
    }
}
