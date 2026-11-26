package com.agussnb.circuitmakerf1.domain.model

import com.agussnb.circuitmakerf1.domain.service.FIA_RACE_DISTANCE_KM

data class AppSettings(
    /** Si está activo, la app usa un recorrido simulado en lugar del GPS real. */
    val simulateGps: Boolean = false,
    /** Distancia de carrera usada para calcular las vueltas (305 km según la FIA). */
    val raceDistanceKm: Double = FIA_RACE_DISTANCE_KM,
    /** Deporte que se asigna a los circuitos nuevos. */
    val sportType: SportType = SportType.CYCLING
)
