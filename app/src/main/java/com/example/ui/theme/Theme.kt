package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioDarkColorScheme = darkColorScheme(
    primary = StudioPrimary,
    onPrimary = Color.White,
    primaryContainer = StudioSurfaceElevated,
    onPrimaryContainer = StudioPrimaryLight,
    secondary = StudioSecondary,
    onSecondary = Color(0xFF0A101D),
    secondaryContainer = StudioSurfaceVariant,
    onSecondaryContainer = StudioSecondaryVariant,
    tertiary = StudioTertiary,
    onTertiary = Color.White,
    background = StudioBackground,
    onBackground = StudioTextPrimary,
    surface = StudioSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioBorder,
    error = StudioError,
    onError = Color.White
)

private val StudioLightColorScheme = lightColorScheme(
    primary = Color(0xFF5843BE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECEBFA),
    onPrimaryContainer = Color(0xFF221765),
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF004D40),
    tertiary = Color(0xFFD63031),
    onTertiary = Color.White,
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF1E272E),
    surface = Color.White,
    onSurface = Color(0xFF1E272E),
    surfaceVariant = Color(0xFFECEFF4),
    onSurfaceVariant = Color(0xFF57606F),
    outline = Color(0xFFDFE4EA),
    error = Color(0xFFD63031),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek studio dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
