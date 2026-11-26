package com.agussnb.circuitmakerf1.domain.model

data class Track(
    val id: Int = 0,
    val name: String,
    val country: String,
    val lengthKm: Double,
    val laps: Int,
    val sportType: SportType = SportType.CYCLING,
    /** Trazado del circuito (ya simplificado). Vacío en circuitos creados antes de guardar puntos. */
    val path: List<GeoPoint> = emptyList()
)
