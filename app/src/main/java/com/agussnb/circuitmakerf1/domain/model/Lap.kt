package com.agussnb.circuitmakerf1.domain.model

/** Una vuelta completa: siempre tiene los 3 tiempos de sector. */
data class Lap(
    val number: Int,
    val sectorTimesMillis: List<Long>
) {
    val lapTimeMillis: Long
        get() = sectorTimesMillis.sum()
}

/** Colores de cronometraje de la F1. */
enum class TimingColor {
    /** Mejor tiempo histórico del usuario. */
    PURPLE,

    /** Mejor tiempo de la sesión actual. */
    GREEN,

    /** Más lento. */
    YELLOW
}
