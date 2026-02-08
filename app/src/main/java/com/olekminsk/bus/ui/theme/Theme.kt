package com.olekminsk.bus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E3C72),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF006C4C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF89F8C7),
    onSecondaryContainer = Color(0xFF002114),
    tertiary = Color(0xFF0061A6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD2E4FF),
    onTertiaryContainer = Color(0xFF001C37),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    inverseOnSurface = Color(0xFFF1F0F4),
    inverseSurface = Color(0xFF2F3033),
    inversePrimary = Color(0xFFA9C7FF),
    surfaceTint = Color(0xFF1E3C72),
    outlineVariant = Color(0xFFC4C6D0),
    scrim = Color(0xFF000000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468C),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF6CDBAC),
    onSecondary = Color(0xFF003826),
    secondaryContainer = Color(0xFF005138),
    onSecondaryContainer = Color(0xFF89F8C7),
    tertiary = Color(0xFF9FCAFF),
    onTertiary = Color(0xFF003259),
    tertiaryContainer = Color(0xFF00497E),
    onTertiaryContainer = Color(0xFFD2E4FF),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    inverseOnSurface = Color(0xFF1A1C1E),
    inverseSurface = Color(0xFFE3E2E6),
    inversePrimary = Color(0xFF1E3C72),
    surfaceTint = Color(0xFFA9C7FF),
    outlineVariant = Color(0xFF44474E),
    scrim = Color(0xFF000000)
)

@Composable
fun BusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
