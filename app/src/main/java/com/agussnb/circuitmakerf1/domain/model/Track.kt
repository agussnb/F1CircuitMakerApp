package com.agussnb.circuitmakerf1.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Track(
    val id: Int = 0,
    val name: String,
    val country: String,
    val lengthKm: Double,
    val laps: Int
) {
    override fun toString(): String {
        return buildString {
            append("--------------------\n")
            append("🏁 Circuito: $name\n")
            append("🌍 País: $country\n")
            append("📏 Longitud: ${"%.3f".format(lengthKm)} km\n")
            append("🔄 Vueltas: $laps\n")
            append("--------------------")
        }
    }
}