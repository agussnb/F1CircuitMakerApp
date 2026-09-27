package com.agussnb.circuitmakerf1.domain.service

const val FIA_RACE_DISTANCE_KM = 305.0

fun fiaLapCount(lengthKm: Double, raceDistanceKm: Double = FIA_RACE_DISTANCE_KM): Int {
    if (lengthKm <= 0) return 0
    return (raceDistanceKm / lengthKm).toInt() + 1
}