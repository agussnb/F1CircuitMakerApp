package com.agussnb.circuitmakerf1.ui.components

import com.agussnb.circuitmakerf1.domain.model.Track

fun Track.toDisplayText(): String = buildString {
    append("--------------------\n")
    append("🏁 Circuito: $name\n")
    append("🌍 País: $country\n")
    append("📏 Longitud: ${"%.3f".format(lengthKm)} km\n")
    append("🔄 Vueltas: $laps\n")
    append("--------------------")
}