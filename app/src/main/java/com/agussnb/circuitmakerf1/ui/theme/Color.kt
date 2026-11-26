package com.agussnb.circuitmakerf1.ui.theme

import androidx.compose.ui.graphics.Color
import com.agussnb.circuitmakerf1.domain.model.TimingColor

// Paleta base, inspirada en la F1
val F1Red = Color(0xFFE10600)
val F1RedDark = Color(0xFFB00500)
val Carbon = Color(0xFF15151E)
val CarbonSurface = Color(0xFF1F1F2B)
val CarbonSurfaceHigh = Color(0xFF2A2A38)
val Asphalt = Color(0xFF9A9AA8)
val OffWhite = Color(0xFFF4F4F6)

// Colores de cronometraje
val TimingPurple = Color(0xFFB138DD)
val TimingGreen = Color(0xFF00C853)
val TimingYellow = Color(0xFFFFD600)

// Colores de sector en el mapa (como en la transmisión de la F1)
val Sector1Color = Color(0xFFE10600)
val Sector2Color = Color(0xFF1E88E5)
val Sector3Color = Color(0xFFFDD835)
val SectorColors = listOf(Sector1Color, Sector2Color, Sector3Color)

fun TimingColor.toColor(): Color = when (this) {
    TimingColor.PURPLE -> TimingPurple
    TimingColor.GREEN -> TimingGreen
    TimingColor.YELLOW -> TimingYellow
}
