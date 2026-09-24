package de.f_soft_studio.abookplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode(val label: String) {
    SYSTEM_DYNAMIC("System (Material You)"),
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
    secondary = PrimaryAmberLight,
    onSecondary = OnDarkPrimary,
    secondaryContainer = Color(0xFF3B3322),
    onSecondaryContainer = PrimaryAmberLight,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color.LightGray,
    outline = Color(0xFF494A54),
    outlineVariant = Color(0xFF2B2C36)
)

val ForestMossColorScheme = darkColorScheme(
    primary = PrimaryEmerald,
    onPrimary = Color.Black,
    primaryContainer = ForestSurfaceVariant,
    onPrimaryContainer = PrimaryEmeraldLight,
    secondary = PrimaryEmeraldLight,
    onSecondary = Color(0xFF00391A),
    background = ForestOledBackground,
    onBackground = Color.White,
    surface = ForestSurface,
    onSurface = Color.White,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = Color.LightGray,
    outline = Color(0xFF384E3C),
    outlineVariant = ForestSurfaceVariant
)

val MidnightVioletColorScheme = darkColorScheme(
    primary = PrimaryNeonViolet,
    onPrimary = Color.White,
    primaryContainer = VioletSurfaceVariant,
    onPrimaryContainer = PrimaryNeonVioletLight,
    secondary = PrimaryNeonVioletLight,
    onSecondary = Color(0xFF24005A),
    background = VioletSpaceBackground,
    onBackground = Color.White,
    surface = VioletSurface,
    onSurface = Color.White,
    surfaceVariant = VioletSurfaceVariant,
    onSurfaceVariant = Color.LightGray,
    outline = Color(0xFF4D4375),
    outlineVariant = VioletSurfaceVariant
)

val WarmPaperColorScheme = lightColorScheme(
    primary = PrimaryAmber,
    onPrimary = WarmTextPrimary, // Hoher Kontrast für WCAG 2.1 AA (dunkles Espresso auf Bernstein)
    primaryContainer = WarmCardBorder,
    onPrimaryContainer = WarmTextPrimary,
    secondary = Color(0xFF8C6D46),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E7D7),
    onSecondaryContainer = WarmTextPrimary,
    background = WarmBackground,
    onBackground = WarmTextPrimary,
    surface = WarmSurface,
    onSurface = WarmTextPrimary,
    surfaceVariant = WarmCardBorder,
    onSurfaceVariant = WarmTextSecondary,
    outline = Color(0xFF8A7F75),
    outlineVariant = WarmCardBorder
)

/**
 * Das zentrale Material 3 Theme der ABook Player Anwendung mit umschaltbaren Themen.
 */
@Composable
fun ABookTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK_STAGE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.SYSTEM_DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                DarkStageColorScheme
            }
        }
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
