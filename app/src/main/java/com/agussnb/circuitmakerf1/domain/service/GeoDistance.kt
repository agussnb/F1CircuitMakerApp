package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

internal fun Double.toRadians(): Double = this * PI / 180.0
internal fun Double.toDegrees(): Double = this * 180.0 / PI

/** Distancia sobre la superficie terrestre (fórmula de Haversine). */
fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
    val lat1 = a.latitude.toRadians()
    val lat2 = b.latitude.toRadians()
    val deltaLat = (b.latitude - a.latitude).toRadians()
    val deltaLon = (b.longitude - a.longitude).toRadians()

    val h = sin(deltaLat / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)

    return 2 * EARTH_RADIUS_METERS * asin(sqrt(h))
}

fun distanceMeters(a: GpsPoint, b: GpsPoint): Double = distanceMeters(a.position, b.position)

/** Rumbo de [from] hacia [to], en grados: 0 = norte, 90 = este, 180 = sur, 270 = oeste. */
fun bearingDegrees(from: GeoPoint, to: GeoPoint): Double {
    val lat1 = from.latitude.toRadians()
    val lat2 = to.latitude.toRadians()
    val deltaLon = (to.longitude - from.longitude).toRadians()

    val y = sin(deltaLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

    return (atan2(y, x).toDegrees() + 360.0) % 360.0
}

/** Punto intermedio entre [a] y [b]. Lineal: alcanza para distancias cortas como las de un circuito. */
fun interpolate(a: GeoPoint, b: GeoPoint, fraction: Double): GeoPoint = GeoPoint(
    latitude = a.latitude + (b.latitude - a.latitude) * fraction,
    longitude = a.longitude + (b.longitude - a.longitude) * fraction
)

/** Desplaza el punto unos metros hacia el este y el norte. */
fun GeoPoint.offsetMeters(eastMeters: Double, northMeters: Double): GeoPoint = GeoPoint(
    latitude = latitude + (northMeters / EARTH_RADIUS_METERS).toDegrees(),
    longitude = longitude + (eastMeters / (EARTH_RADIUS_METERS * cos(latitude.toRadians()))).toDegrees()
)

/**
 * Coordenadas planas en metros (x = este, y = norte) relativas a un origen.
 *
 * En distancias de pocos kilómetros la Tierra es "plana" a los fines prácticos, y en un plano
 * las cuentas de geometría (intersecciones, distancia a una recta) son mucho más simples.
 */
internal data class PlanarPoint(val x: Double, val y: Double)

internal fun GeoPoint.toPlanar(origin: GeoPoint): PlanarPoint = PlanarPoint(
    x = (longitude - origin.longitude).toRadians() * EARTH_RADIUS_METERS * cos(origin.latitude.toRadians()),
    y = (latitude - origin.latitude).toRadians() * EARTH_RADIUS_METERS
)
