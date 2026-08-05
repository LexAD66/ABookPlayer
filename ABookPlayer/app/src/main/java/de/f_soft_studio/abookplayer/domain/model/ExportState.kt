package de.f_soft_studio.abookplayer.domain.model

sealed class ExportState {
    object Idle : ExportState()
    data class Exporting(val progressPercent: Int) : ExportState()
    data class Success(val message: String) : ExportState()
    data class Error(val errorMessage: String) : ExportState()
}
