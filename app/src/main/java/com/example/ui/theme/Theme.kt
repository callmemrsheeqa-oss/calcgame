package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryYellow,
    onPrimary = DeepCharcoal,
    primaryContainer = SecondaryYellow,
    onPrimaryContainer = DeepCharcoal,
    secondary = SecondaryYellow,
    onSecondary = DeepCharcoal,
    background = DeepCharcoal,
    onBackground = OffWhite,
    surface = DarkGrey,
    onSurface = OffWhite,
    surfaceVariant = MidGrey,
    onSurfaceVariant = OffWhite,
    outline = MidGrey,
    error = SnakeRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryYellow,
    onPrimary = DeepCharcoal,
    primaryContainer = SecondaryYellow,
    onPrimaryContainer = DeepCharcoal,
    secondary = SecondaryYellow,
    onSecondary = DeepCharcoal,
    background = Color(0xFFFAFAFA),
    onBackground = DeepCharcoal,
    surface = Color.White,
    onSurface = DeepCharcoal,
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = DeepCharcoal,
    outline = Color(0xFFE5E5EA),
    error = SnakeRed,
    onError = Color.White
)

@Composable
fun CalcSnakeTheme(
    darkTheme: Boolean = true, // Default to dark for sleek high-contrast yellow/charcoal look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
