package com.agussnb.circuitmakerf1.domain.model

/** Una posición en el mapa, sin precisión ni tiempo. Es lo que se guarda del trazado. */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)
