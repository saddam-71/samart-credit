package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = DarkNavy,
    primaryContainer = PrimaryTeal,
    onPrimaryContainer = Color.White,
    secondary = SkyAction,
    onSecondary = Color.White,
    background = DarkNavy,
    onBackground = Color.White,
    surface = NavyLight,
    onSurface = Color.White,
    surfaceVariant = NavyLighter,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = CrimsonAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondary = DarkNavy,
    onSecondary = Color.White,
    background = SlateLight,
    onBackground = TextPrimaryDark,
    surface = CardSurfaceWhite,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryMuted,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
