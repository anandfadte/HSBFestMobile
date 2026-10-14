package org.zenith26.fest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple900,
    onPrimaryContainer = Purple300,
    secondary = Cyan300,
    onSecondary = Slate950,
    secondaryContainer = Cyan950,
    onSecondaryContainer = Cyan300,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate750,
    error = Rose500,
    onError = Color.White,
    errorContainer = Rose950,
    onErrorContainer = Rose300
)

@Composable
fun ZenithFestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
