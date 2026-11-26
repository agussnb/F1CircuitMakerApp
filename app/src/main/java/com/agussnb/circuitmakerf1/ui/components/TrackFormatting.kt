package com.agussnb.circuitmakerf1.ui.components

import com.agussnb.circuitmakerf1.domain.model.SportType
import kotlin.math.abs

/** "1:23.456" (o "23.456" si dura menos de un minuto). */
fun formatLapTime(millis: Long?): String {
    if (millis == null) return "--:--.---"
    val total = abs(millis)
    val minutes = total / 60_000
    val seconds = (total % 60_000) / 1000
    val thousandths = total % 1000
    val sign = if (millis < 0) "-" else ""
    return if (minutes > 0) {
        "$sign$minutes:${seconds.pad(2)}.${thousandths.pad(3)}"
    } else {
        "$sign$seconds.${thousandths.pad(3)}"
    }
}

/** "+0.523" / "-1.204": negativo = más rápido que la referencia. */
fun formatDelta(millis: Long): String {
    val sign = if (millis < 0) "-" else "+"
    return sign + formatLapTime(abs(millis))
}

/** Formato con coma decimal, como se escribe en Argentina: "1,234 km". */
fun formatKm(km: Double, decimals: Int = 3): String =
    "%.${decimals}f".format(km).replace('.', ',') + " km"

fun SportType.label(): String = when (this) {
    SportType.CYCLING -> "Ciclismo"
    SportType.RUNNING -> "Atletismo"
}

fun SportType.emoji(): String = when (this) {
    SportType.CYCLING -> "🚴"
    SportType.RUNNING -> "🏃"
}

private fun Long.pad(length: Int): String = toString().padStart(length, '0')
