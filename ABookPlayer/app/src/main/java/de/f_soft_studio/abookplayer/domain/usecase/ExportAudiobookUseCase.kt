package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.Chapter
import de.f_soft_studio.abookplayer.domain.model.ExportState
import de.f_soft_studio.abookplayer.storage.AbookStorage
import kotlinx.coroutines.flow.Flow
import java.io.OutputStream

class ExportAudiobookUseCase(
    private val abookStorage: AbookStorage
) {
    operator fun invoke(
        audiobook: Audiobook,
        chapters: List<Chapter>,
        outputStream: OutputStream
    ): Flow<ExportState> {
        return abookStorage.exportToAbook(audiobook, chapters, outputStream)
    }
}
