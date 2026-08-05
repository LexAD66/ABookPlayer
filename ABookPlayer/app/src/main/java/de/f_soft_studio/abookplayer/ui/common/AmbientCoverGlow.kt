package de.f_soft_studio.abookplayer.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * AmbientCoverGlow: Erzeugt einen weichen, immersiven Umgebungslicht-Glow hinter Medien-Covern.
 */
@Composable
fun AmbientCoverGlow(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        // Outer Radial Blur Aura
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(8.dp)
                .blur(28.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.55f),
                            glowColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}
