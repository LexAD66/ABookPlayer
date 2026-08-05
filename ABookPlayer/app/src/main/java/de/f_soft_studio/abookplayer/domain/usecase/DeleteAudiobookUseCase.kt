package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository

/**
 * UseCase zum Löschen eines Hörbuchs aus der Datenbank.
 * Verknüpfte Kapitel und Lesezeichen werden automatisch via Room CASCADE gelöscht.
 */
class DeleteAudiobookUseCase(
    private val repository: AudiobookRepository
) {
    suspend operator fun invoke(audiobookId: Long) {
        repository.deleteAudiobook(audiobookId)
    }
}
