package com.agussnb.circuitmakerf1.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Track(
    val id: Int = 0,
    val name: String,
    val country: String,
    val lengthKm: Double,
    val laps: Int
)

