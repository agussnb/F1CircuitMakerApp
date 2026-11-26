package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import kotlin.math.hypot

const val DEFAULT_SIMPLIFY_TOLERANCE_METERS = 2.0

/**
 * Simplifica una línea con el algoritmo de Douglas-Peucker: saca los puntos que se apartan
 * menos de [toleranceMeters] de la recta entre sus vecinos. Las rectas quedan con 2 puntos
 * y las curvas conservan los necesarios para mantener la forma.
 */
fun simplifyPath(
    points: List<GeoPoint>,
    toleranceMeters: Double = DEFAULT_SIMPLIFY_TOLERANCE_METERS
): List<GeoPoint> {
    if (points.size < 3) return points

    val origin = points.first()
    val planar = points.map { it.toPlanar(origin) }
    val keep = BooleanArray(points.size)
    keep[0] = true
    keep[points.lastIndex] = true

    // Versión iterativa (con una pila) para no desbordar la pila de llamadas con trazados largos.
    val pending = ArrayDeque<Pair<Int, Int>>()
    pending.addLast(0 to points.lastIndex)

    while (pending.isNotEmpty()) {
        val (start, end) = pending.removeLast()
        var maxDistance = 0.0
        var farthest = -1

        for (i in start + 1 until end) {
            val distance = distanceToSegment(planar[i], planar[start], planar[end])
            if (distance > maxDistance) {
                maxDistance = distance
                farthest = i
            }
        }

        if (farthest != -1 && maxDistance > toleranceMeters) {
            keep[farthest] = true
            pending.addLast(start to farthest)
            pending.addLast(farthest to end)
        }
    }

    return points.filterIndexed { index, _ -> keep[index] }
}

private fun distanceToSegment(p: PlanarPoint, a: PlanarPoint, b: PlanarPoint): Double {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val lengthSquared = dx * dx + dy * dy
    if (lengthSquared == 0.0) return hypot(p.x - a.x, p.y - a.y)

    val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSquared).coerceIn(0.0, 1.0)
    return hypot(p.x - (a.x + t * dx), p.y - (a.y + t * dy))
}
