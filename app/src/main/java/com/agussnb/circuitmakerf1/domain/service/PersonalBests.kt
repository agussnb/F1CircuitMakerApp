package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.Lap
import com.agussnb.circuitmakerf1.domain.model.TimingColor

data class PersonalBests(
    val bestLapMillis: Long? = null,
    val bestSectorsMillis: List<Long?> = List(SECTOR_COUNT) { null },
    val totalLaps: Int = 0
) {
    /** Vuelta ideal: la suma de tus mejores sectores, aunque sean de vueltas distintas. */
    val idealLapMillis: Long?
        get() = if (bestSectorsMillis.all { it != null }) bestSectorsMillis.sumOf { it!! } else null
}

fun personalBests(laps: List<Lap>): PersonalBests = PersonalBests(
    bestLapMillis = laps.minOfOrNull { it.lapTimeMillis },
    bestSectorsMillis = List(SECTOR_COUNT) { sector ->
        laps.mapNotNull { it.sectorTimesMillis.getOrNull(sector) }.minOrNull()
    },
    totalLaps = laps.size
)

/**
 * Color F1 de un tiempo (de sector o de vuelta), comparándolo con los mejores **anteriores**:
 * - 🟣 si iguala o mejora tu mejor histórico (o es el primero),
 * - 🟢 si mejora tu mejor de esta sesión,
 * - 🟡 si es más lento.
 */
fun classifyTime(timeMillis: Long, sessionBestMillis: Long?, overallBestMillis: Long?): TimingColor =
    when {
        overallBestMillis == null || timeMillis <= overallBestMillis -> TimingColor.PURPLE
        sessionBestMillis == null || timeMillis <= sessionBestMillis -> TimingColor.GREEN
        else -> TimingColor.YELLOW
    }
