package com.agussnb.circuitmakerf1.domain.model

/**
 * Lectura del GPS.
 *
 * [timestampMillis] es un reloj **monotónico** (en Android, `elapsedRealtime`): no salta
 * si el usuario cambia la hora del teléfono, y sirve para medir tiempos de vuelta.
 */
data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long
) {
    val position: GeoPoint
        get() = GeoPoint(latitude, longitude)
}
