package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository

/**
 * UseCase zum Speichern des Wiedergabefortschritts eines Hörbuchs
 * und Aufzeichnen der tatsächlichen Hörzeit für das Statistik-Dashboard.
 */
class SaveProgressUseCase(
    private val repository: AudiobookRepository,
    private val recordListeningTimeUseCase: RecordListeningTimeUseCase? = null
) {
    private val lastRecordedPositionMap = mutableMapOf<Long, Long>()

    /**
     * Speichert den aktuellen Fortschritt und verbucht die abgespielte Zeit in den Statistiken.
     *
     * @param audiobookId ID des aktiven Hörbuchs.
     * @param currentPosition Aktuelle Abspielposition in Millisekunden.
     */
    suspend operator fun invoke(audiobookId: Long, currentPosition: Long) {
        val lastPos = lastRecordedPositionMap[audiobookId]
        if (lastPos != null && currentPosition > lastPos) {
            val deltaMs = currentPosition - lastPos
            // Nur normale Wiedergabefortschritte (<= 30 s) zählen, keine großen Sprünge/Seeks
            if (deltaMs in 1..30_000L) {
                recordListeningTimeUseCase?.invoke(audiobookId, deltaMs / 1000L)
            }
        }
        lastRecordedPositionMap[audiobookId] = currentPosition
        repository.updateProgress(audiobookId, currentPosition)
    }
}
