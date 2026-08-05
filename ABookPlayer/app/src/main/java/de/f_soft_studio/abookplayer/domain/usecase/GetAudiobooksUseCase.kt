package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import kotlinx.coroutines.flow.Flow

/**
 * UseCase zum Abrufen aller Hörbücher aus dem Repository.
 */
class GetAudiobooksUseCase(
    private val repository: AudiobookRepository
) {
    operator fun invoke(): Flow<List<Audiobook>> {
        return repository.getAllAudiobooks()
    }
}
