package de.f_soft_studio.abookplayer.domain.usecase

import android.net.Uri
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.storage.AbookStorage

/**
 * UseCase zum Scannen und Importieren von .abook-Hörbüchern über AbookStorage.
 */
class ImportAudiobookUseCase(
    private val storage: AbookStorage
) {
    /**
     * Scannt den Speicher (/Download/ und /Download/ABook/) und importiert gefundene Hörbücher.
     */
    suspend fun scanAndImport(): List<Audiobook> {
        return storage.scanAndImport()
    }

    /**
     * Importiert eine .abook-Datei über den Storage Access Framework (SAF) Picker.
     */
    suspend fun importFromUri(uri: Uri): Audiobook? {
        return storage.importFromUri(uri)
    }
}
