package com.virginactive.android.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VaLightColorScheme = lightColorScheme(
    primary = VaRed,
    onPrimary = White,
    background = White,
    onBackground = OnSurface,
    surface = White,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorRed,
    onError = White,
)

@Composable
fun VaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VaLightColorScheme,
        typography = VaTypography,
        content = content,
    )
}
