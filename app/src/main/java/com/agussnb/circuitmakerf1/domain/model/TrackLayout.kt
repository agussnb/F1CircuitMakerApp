package com.agussnb.circuitmakerf1.domain.model

/**
 * Línea de cronometraje (meta o fin de sector): un punto del circuito más la dirección
 * de carrera en ese punto. La línea en sí es perpendicular a esa dirección.
 */
data class TimingGate(
    val position: GeoPoint,
    val bearingDegrees: Double,
    val halfWidthMeters: Double
)

/**
 * El circuito "armado": se calcula a partir del trazado guardado.
 *
 * - [gates]\[0] es la línea de meta, [gates]\[1] el fin del sector 1 y [gates]\[2] el fin del sector 2.
 * - [sectors] tiene el tramo de cada sector, para dibujarlos en el mapa.
 */
data class TrackLayout(
    val lengthMeters: Double,
    val gates: List<TimingGate>,
    val sectors: List<List<GeoPoint>>
)
