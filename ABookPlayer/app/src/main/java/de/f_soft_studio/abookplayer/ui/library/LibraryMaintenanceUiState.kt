package de.f_soft_studio.abookplayer.ui.library

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository.LibraryCleanupResult
import de.f_soft_studio.abookplayer.util.DuplicateMatch

/**
 * Expliziter Zustand für Bibliotheks-Wartung (Scan, Cleanup, Duplikate).
 * Trennt „läuft" / „Ergebnis" / „Duplikate", damit die UI Prozesse darstellen
 * kann, ohne die normale Bibliothek zu blockieren.
 */
data class LibraryMaintenanceUiState(
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val scanProgressText: String? = null,
    val lastScanMessage: String? = null,
    val duplicates: List<DuplicateMatch> = emptyList(),
    val cleanupResult: LibraryCleanupResult? = null
)
