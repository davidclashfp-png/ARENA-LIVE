package com.arenalive.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ArenaColors {
    val Background = Color(0xFF0B1011)
    val Surface = Color(0xFF141C1D)
    val SurfaceAlt = Color(0xFF1A2425)
    val SurfaceSoft = Color(0xFF202C2D)
    val Lime = Color(0xFFC6F36A)
    val LimeDim = Color(0xFF78963F)
    val Text = Color(0xFFF3F7F4)
    val Muted = Color(0xFF91A09E)
    val Divider = Color(0xFF293536)
    val Red = Color(0xFFFF6B70)
    val Blue = Color(0xFF77B9FF)
}

@Composable
fun ArenaTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = ArenaColors.Lime,
        onPrimary = ArenaColors.Background,
        background = ArenaColors.Background,
        onBackground = ArenaColors.Text,
        surface = ArenaColors.Surface,
        onSurface = ArenaColors.Text,
        secondary = ArenaColors.Blue,
        onSurfaceVariant = ArenaColors.Muted,
        outline = ArenaColors.Divider
    )
    MaterialTheme(colorScheme = colors, content = content)
}
