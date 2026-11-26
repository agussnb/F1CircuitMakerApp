package com.agussnb.circuitmakerf1.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Un punto del trazado de un circuito. La clave primaria es (trackId, position):
 * `position` es el orden del punto dentro del trazado.
 */
@Entity(
    tableName = "track_point",
    primaryKeys = ["trackId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackId")]
)
data class TrackPointEntity(
    val trackId: Int,
    val position: Int,
    val latitude: Double,
    val longitude: Double
)
