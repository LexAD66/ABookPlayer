package de.f_soft_studio.abookplayer.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Verfügbare Farbschemata / Themes der ABook Player App.
 */
enum class AppTheme(val displayName: String) {
    WARM_PAPER("Warm Paper (Standard)"),
    OLED_TRUE_BLACK("OLED True Black (#000000)"),
    DARK_STAGE("Dunkles Bühnen-Design");

    companion object {
        val OledBackgroundColor = Color(0xFF000000)
        val OledSurfaceColor = Color(0xFF121212)
        val OledCardColor = Color(0xFF1E1E1E)
    }
}
