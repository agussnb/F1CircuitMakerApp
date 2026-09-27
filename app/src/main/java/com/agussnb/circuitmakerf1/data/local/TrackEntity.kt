package com.agussnb.circuitmakerf1.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val country: String,
    val lengthKm: Double,
    val laps: Int
)