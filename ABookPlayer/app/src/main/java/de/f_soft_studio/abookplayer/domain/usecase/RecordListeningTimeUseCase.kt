package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository

/**
 * UseCase zum Aufzeichnen von Hörsitzungen.
 */
class RecordListeningTimeUseCase(
    private val repository: AudiobookRepository
) {
    suspend operator fun invoke(audiobookId: Long, durationSeconds: Long) {
        if (durationSeconds > 0) {
            repository.recordListeningSession(audiobookId, durationSeconds)
        }
    }
}
