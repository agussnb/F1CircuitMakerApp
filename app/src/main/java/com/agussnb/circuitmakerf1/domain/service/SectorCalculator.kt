package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import com.agussnb.circuitmakerf1.domain.model.TimingGate
import com.agussnb.circuitmakerf1.domain.model.TrackLayout
import kotlin.math.cos
import kotlin.math.sin

const val SECTOR_COUNT = 3

/**
 * Medio ancho de cada línea de cronometraje. Es generoso a propósito: el GPS de un
 * teléfono puede errar 5–10 m, y la línea tiene que "atraparte" igual.
 */
const val GATE_HALF_WIDTH_METERS = 25.0

private const val MIN_TRACK_LENGTH_METERS = 30.0

/**
 * Arma el circuito a partir del trazado: lo divide en [SECTOR_COUNT] sectores de igual
 * distancia y ubica la línea de meta (en el primer punto) y las líneas de fin de sector.
 *
 * Devuelve `null` si el trazado es demasiado corto para ser un circuito.
 */
fun buildTrackLayout(path: List<GeoPoint>): TrackLayout? {
    if (path.size < 2) return null
    val walker = runCatching { PathWalker(path) }.getOrNull() ?: return null
    if (walker.lengthMeters < MIN_TRACK_LENGTH_METERS) return null

    val sectorLength = walker.lengthMeters / SECTOR_COUNT
    val boundaries = (0 until SECTOR_COUNT).map { it * sectorLength }

    val gates = boundaries.map { distance ->
        TimingGate(
            position = walker.positionAt(distance),
            bearingDegrees = walker.directionAt(distance),
            halfWidthMeters = GATE_HALF_WIDTH_METERS
        )
    }

    val sectors = boundaries.map { start ->
        walker.subPath(start, start + sectorLength)
    }

    return TrackLayout(
        lengthMeters = walker.lengthMeters,
        gates = gates,
        sectors = sectors
    )
}

/** Los dos extremos de la línea de cronometraje, para dibujarla en el mapa. */
fun TimingGate.endpoints(): Pair<GeoPoint, GeoPoint> {
    val bearing = bearingDegrees.toRadians()
    // Dirección perpendicular al rumbo de carrera (90° a la derecha).
    val east = cos(bearing) * halfWidthMeters
    val north = -sin(bearing) * halfWidthMeters
    return position.offsetMeters(-east, -north) to position.offsetMeters(east, north)
}
