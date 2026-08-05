package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Bookmark

/**
 * UseCase zum Erstellen eines neuen Lesezeichens für ein Hörbuch.
 */
class AddBookmarkUseCase(
    private val repository: AudiobookRepository
) {
    suspend operator fun invoke(
        audiobookId: Long,
        position: Long,
        note: String = ""
    ): Long {
        val bookmark = Bookmark(
            audiobookId = audiobookId,
            position = position,
            note = note.ifBlank { "Lesezeichen" },
            createdAt = System.currentTimeMillis()
        )
        return repository.addBookmark(bookmark)
    }
}
