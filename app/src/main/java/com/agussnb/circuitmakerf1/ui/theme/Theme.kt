package com.agussnb.circuitmakerf1.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema oscuro fijo, estilo pantalla de cronometraje de la F1. No usamos los colores
 * dinámicos de Android 12+ para que la app se vea igual en todos los teléfonos.
 */
private val F1ColorScheme = darkColorScheme(
    primary = F1Red,
    onPrimary = Color.White,
    primaryContainer = F1RedDark,
    onPrimaryContainer = Color.White,
    secondary = Asphalt,
    onSecondary = Carbon,
    tertiary = TimingPurple,
    background = Carbon,
    onBackground = OffWhite,
    surface = Carbon,
    onSurface = OffWhite,
    surfaceVariant = CarbonSurfaceHigh,
    onSurfaceVariant = Asphalt,
    surfaceContainer = CarbonSurface,
    surfaceContainerHigh = CarbonSurfaceHigh,
    surfaceContainerHighest = CarbonSurfaceHigh,
    surfaceContainerLow = CarbonSurface,
    outline = Asphalt
)

@Composable
fun CircuitMakerF1Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = F1ColorScheme,
        typography = Typography,
        content = content
    )
}
