package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CinematicColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    onPrimary = Color.Black,
    secondary = SecondaryNeon,
    onSecondary = Color.Black,
    tertiary = TertiaryNeon,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDarkSecondary,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinematicColorScheme,
        typography = Typography,
        content = content
    )
}
