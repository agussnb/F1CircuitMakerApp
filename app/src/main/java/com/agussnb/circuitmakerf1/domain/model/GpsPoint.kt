package com.agussnb.circuitmakerf1.domain.model

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long
)