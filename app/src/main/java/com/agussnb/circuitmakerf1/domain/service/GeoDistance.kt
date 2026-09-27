package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

private fun Double.toRadians(): Double = this * PI / 180.0

fun distanceMeters(a: GpsPoint, b: GpsPoint): Double {
    val lat1 = a.latitude.toRadians()
    val lat2 = b.latitude.toRadians()
    val deltaLat = (b.latitude - a.latitude).toRadians()
    val deltaLon = (b.longitude - a.longitude).toRadians()

    val h = sin(deltaLat / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)

    return 2 * EARTH_RADIUS_METERS * asin(sqrt(h))
}