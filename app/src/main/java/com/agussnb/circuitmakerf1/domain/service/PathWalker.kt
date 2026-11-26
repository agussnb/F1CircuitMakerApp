package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import kotlin.math.min

/**
 * Permite "caminar" un circuito cerrado: dada una distancia desde la largada, devuelve
 * la posición y el rumbo en ese punto. Si la distancia supera una vuelta, sigue dando vueltas.
 *
 * Lo usan el cálculo de sectores y el GPS simulado.
 */
class PathWalker(path: List<GeoPoint>) {

    /** El trazado cerrado: el último punto es igual al primero. */
    val points: List<GeoPoint> = closeLoop(path)

    private val cumulative: DoubleArray = DoubleArray(points.size).also { acc ->
        for (i in 1 until points.size) {
            acc[i] = acc[i - 1] + distanceMeters(points[i - 1], points[i])
        }
    }

    val lengthMeters: Double = cumulative.last()

    init {
        require(lengthMeters > 0.0) { "El trazado necesita al menos dos puntos distintos" }
    }

    fun positionAt(distanceMeters: Double): GeoPoint {
        val (segment, fraction) = locate(distanceMeters)
        return interpolate(points[segment], points[segment + 1], fraction)
    }

    fun bearingAt(distanceMeters: Double): Double {
        var (segment, _) = locate(distanceMeters)
        // Si el tramo mide cero (puntos repetidos), usamos el siguiente que tenga largo.
        repeat(points.size) {
            if (cumulative[segment + 1] > cumulative[segment]) {
                return bearingDegrees(points[segment], points[segment + 1])
            }
            segment = (segment + 1) % (points.size - 1)
        }
        return 0.0
    }

    /**
     * Dirección "suavizada" del circuito en ese punto: el rumbo de la cuerda que va de
     * [windowMeters] antes a [windowMeters] después. En una recta da lo mismo que [bearingAt];
     * en una esquina da la bisectriz, así una línea de meta puesta justo en la esquina
     * se puede cruzar viniendo del tramo anterior.
     */
    fun directionAt(distanceMeters: Double, windowMeters: Double = 10.0): Double {
        val window = min(windowMeters, lengthMeters / 4)
        val from = positionAt(distanceMeters - window)
        val to = positionAt(distanceMeters + window)
        return if (from == to) bearingAt(distanceMeters) else bearingDegrees(from, to)
    }

    /** Tramo del circuito entre dos distancias (con `from < to`, dentro de una misma vuelta). */
    fun subPath(fromMeters: Double, toMeters: Double): List<GeoPoint> {
        val inside = points.indices
            .filter { cumulative[it] > fromMeters && cumulative[it] < toMeters }
            .map { points[it] }
        return listOf(positionAt(fromMeters)) + inside + positionAtClamped(toMeters)
    }

    private fun positionAtClamped(distanceMeters: Double): GeoPoint =
        if (distanceMeters >= lengthMeters) points.last() else positionAt(distanceMeters)

    /** Devuelve el índice del tramo y qué fracción de ese tramo se recorrió. */
    private fun locate(distanceMeters: Double): Pair<Int, Double> {
        val d = ((distanceMeters % lengthMeters) + lengthMeters) % lengthMeters

        // Búsqueda binaria: el tramo i va de cumulative[i] a cumulative[i + 1].
        var low = 0
        var high = points.size - 2
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (cumulative[mid] <= d) low = mid else high = mid - 1
        }

        val segmentLength = cumulative[low + 1] - cumulative[low]
        val fraction = if (segmentLength > 0.0) (d - cumulative[low]) / segmentLength else 0.0
        return low to fraction
    }

    private companion object {
        fun closeLoop(path: List<GeoPoint>): List<GeoPoint> {
            require(path.size >= 2) { "El trazado necesita al menos dos puntos" }
            return if (path.first() == path.last()) path else path + path.first()
        }
    }
}
