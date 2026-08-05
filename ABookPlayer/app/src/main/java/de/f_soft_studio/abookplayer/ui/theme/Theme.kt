package de.f_soft_studio.abookplayer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val label: String) {
    DARK_STAGE("Dark Stage"),
    FOREST_MOSS("Smaragd Wald"),
    MIDNIGHT_VIOLET("Mitternacht Violett"),
    WARM_PAPER("Warmes Papier")
}

val DarkStageColorScheme = darkColorScheme(
    primary = PrimaryAmber,
    onPrimary = OnDarkPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = PrimaryAmberLight,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color.LightGray
)

val ForestMossColorScheme = darkColorScheme(
    primary = PrimaryEmerald,
    onPrimary = Color.Black,
    primaryContainer = ForestSurfaceVariant,
    onPrimaryContainer = PrimaryEmeraldLight,
    background = ForestOledBackground,
    onBackground = Color.White,
    surface = ForestSurface,
    onSurface = Color.White,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = Color.LightGray
)

val MidnightVioletColorScheme = darkColorScheme(
    primary = PrimaryNeonViolet,
    onPrimary = Color.White,
    primaryContainer = VioletSurfaceVariant,
    onPrimaryContainer = PrimaryNeonVioletLight,
    background = VioletSpaceBackground,
    onBackground = Color.White,
    surface = VioletSurface,
    onSurface = Color.White,
    surfaceVariant = VioletSurfaceVariant,
    onSurfaceVariant = Color.LightGray
)

val WarmPaperColorScheme = lightColorScheme(
    primary = PrimaryAmber,
    onPrimary = Color.White,
    primaryContainer = WarmCardBorder,
    onPrimaryContainer = WarmTextPrimary,
    background = WarmBackground,
    onBackground = WarmTextPrimary,
    surface = WarmSurface,
    onSurface = WarmTextPrimary,
    surfaceVariant = WarmCardBorder,
    onSurfaceVariant = WarmTextSecondary
)

/**
 * Das zentrale Material 3 Theme der ABook Player Anwendung mit umschaltbaren Themen.
 */
@Composable
fun ABookTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK_STAGE,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.DARK_STAGE -> DarkStageColorScheme
        AppThemeMode.FOREST_MOSS -> ForestMossColorScheme
        AppThemeMode.MIDNIGHT_VIOLET -> MidnightVioletColorScheme
        AppThemeMode.WARM_PAPER -> WarmPaperColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
