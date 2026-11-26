package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import com.agussnb.circuitmakerf1.domain.model.Lap
import com.agussnb.circuitmakerf1.domain.model.TimingColor
import com.agussnb.circuitmakerf1.domain.model.TrackLayout

/** Saltos del GPS más grandes que esto entre dos lecturas se ignoran (no son movimiento real). */
const val MAX_GPS_JUMP_METERS = 150.0

enum class TimingPhase {
    /** Todavía no se cruzó la meta por primera vez (vuelta de salida). */
    WAITING_FOR_START,

    /** Hay una vuelta en curso. */
    RUNNING
}

data class SectorResult(val timeMillis: Long, val color: TimingColor)

data class CompletedLap(
    val number: Int,
    val sectors: List<SectorResult>,
    val lapTimeMillis: Long,
    val lapColor: TimingColor,
    /** Diferencia contra la mejor vuelta anterior (negativo = más rápido). */
    val deltaToBestMillis: Long?
)

data class LapTimingSnapshot(
    val phase: TimingPhase = TimingPhase.WAITING_FOR_START,
    val lapStartMillis: Long? = null,
    val sectorStartMillis: Long? = null,
    /** 0, 1 o 2. */
    val currentSectorIndex: Int = 0,
    val currentLapSectors: List<SectorResult> = emptyList(),
    /** La más reciente al final. */
    val completedLaps: List<CompletedLap> = emptyList(),
    /** Delta de la última línea cruzada contra la mejor vuelta. */
    val lastDeltaMillis: Long? = null,
    val bestLapMillis: Long? = null,
    val bestSectorsMillis: List<Long?> = List(SECTOR_COUNT) { null },
    val position: GeoPoint? = null
)

/**
 * El cronómetro: recibe lecturas GPS y detecta cruces de meta y de sectores.
 *
 * Es Kotlin puro y sin corrutinas: le das puntos con [onPoint] y lees [snapshot].
 * Así se puede testear con recorridos inventados, sin teléfono.
 *
 * @param historicLaps vueltas de sesiones anteriores, para saber cuál es el violeta.
 */
class LapTimingEngine(
    private val layout: TrackLayout,
    historicLaps: List<Lap> = emptyList()
) {
    private val overallBestSectors: MutableList<Long?> =
        personalBests(historicLaps).bestSectorsMillis.toMutableList()
    private var overallBestLap: Lap? = historicLaps.minByOrNull { it.lapTimeMillis }

    private val sessionBestSectors = MutableList<Long?>(SECTOR_COUNT) { null }
    private var sessionBestLapMillis: Long? = null

    private val currentSectorTimes = mutableListOf<Long>()
    private var previous: GpsPoint? = null

    var snapshot = LapTimingSnapshot(
        bestLapMillis = overallBestLap?.lapTimeMillis,
        bestSectorsMillis = overallBestSectors.toList()
    )
        private set

    /** Procesa una lectura. Si con ella se completó una vuelta, la devuelve (para guardarla). */
    fun onPoint(point: GpsPoint): Lap? {
        if (point.accuracyMeters > MAX_ACCURACY_METERS) return null

        val prev = previous
        previous = point
        snapshot = snapshot.copy(position = point.position)

        if (prev == null || distanceMeters(prev, point) > MAX_GPS_JUMP_METERS) return null

        return when (snapshot.phase) {
            TimingPhase.WAITING_FOR_START -> {
                crossingTimeMillis(prev, point, layout.gates[0])?.let { startLap(it) }
                null
            }
            TimingPhase.RUNNING -> onRunningSegment(prev, point)
        }
    }

    private fun onRunningSegment(prev: GpsPoint, point: GpsPoint): Lap? {
        val sector = snapshot.currentSectorIndex
        // La línea que cierra el sector actual: fin de S1, fin de S2, o la meta para S3.
        val nextGate = layout.gates[(sector + 1) % SECTOR_COUNT]

        val crossing = crossingTimeMillis(prev, point, nextGate)
        if (crossing != null) return completeSector(sector, crossing)

        // Si se cruzó la meta sin pasar por todos los sectores (cortaste camino o falló el GPS),
        // la vuelta no vale y empieza una nueva.
        if (sector != SECTOR_COUNT - 1) {
            crossingTimeMillis(prev, point, layout.gates[0])?.let { startLap(it) }
        }
        return null
    }

    private fun startLap(atMillis: Long) {
        currentSectorTimes.clear()
        snapshot = snapshot.copy(
            phase = TimingPhase.RUNNING,
            lapStartMillis = atMillis,
            sectorStartMillis = atMillis,
            currentSectorIndex = 0,
            currentLapSectors = emptyList()
        )
    }

    private fun completeSector(sector: Int, atMillis: Long): Lap? {
        val time = atMillis - (snapshot.sectorStartMillis ?: atMillis)

        val color = classifyTime(time, sessionBestSectors[sector], overallBestSectors[sector])
        if (overallBestSectors[sector]?.let { time < it } != false) overallBestSectors[sector] = time
        if (sessionBestSectors[sector]?.let { time < it } != false) sessionBestSectors[sector] = time

        currentSectorTimes += time
        val sectors = snapshot.currentLapSectors + SectorResult(time, color)

        // Delta contra la mejor vuelta, acumulado hasta este sector.
        val bestUntilHere = overallBestLap?.sectorTimesMillis?.take(sector + 1)?.sum()
        val delta = bestUntilHere?.let { currentSectorTimes.sum() - it }

        if (sector < SECTOR_COUNT - 1) {
            snapshot = snapshot.copy(
                sectorStartMillis = atMillis,
                currentSectorIndex = sector + 1,
                currentLapSectors = sectors,
                lastDeltaMillis = delta,
                bestSectorsMillis = overallBestSectors.toList()
            )
            return null
        }

        val lap = Lap(
            number = snapshot.completedLaps.size + 1,
            sectorTimesMillis = currentSectorTimes.toList()
        )
        val lapColor = classifyTime(lap.lapTimeMillis, sessionBestLapMillis, overallBestLap?.lapTimeMillis)
        if (overallBestLap?.let { lap.lapTimeMillis < it.lapTimeMillis } != false) overallBestLap = lap
        if (sessionBestLapMillis?.let { lap.lapTimeMillis < it } != false) sessionBestLapMillis = lap.lapTimeMillis

        snapshot = snapshot.copy(
            completedLaps = snapshot.completedLaps + CompletedLap(
                number = lap.number,
                sectors = sectors,
                lapTimeMillis = lap.lapTimeMillis,
                lapColor = lapColor,
                deltaToBestMillis = delta
            ),
            lastDeltaMillis = delta,
            bestLapMillis = overallBestLap?.lapTimeMillis,
            bestSectorsMillis = overallBestSectors.toList()
        )
        // La meta es a la vez el final de esta vuelta y el inicio de la siguiente.
        startLap(atMillis)
        return lap
    }
}
