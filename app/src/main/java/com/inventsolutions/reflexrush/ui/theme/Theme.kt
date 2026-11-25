package com.inventsolutions.reflexrush.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ReflexRushColorScheme = darkColorScheme(
    primary = RushGreen,
    onPrimary = RushBackground,
    secondary = RushAmber,
    onSecondary = RushBackground,
    tertiary = RushRed,
    background = RushBackground,
    onBackground = RushOnBackground,
    surface = RushSurface,
    onSurface = RushOnSurface,
    surfaceVariant = RushSurfaceVariant,
    onSurfaceVariant = RushMint
)

@Composable
fun ReflexRushTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReflexRushColorScheme,
        typography = Typography,
        content = content
    )
}
