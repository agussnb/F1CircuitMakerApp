package com.agussnb.circuitmakerf1.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session",
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
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackId: Int,
    val startedAtMillis: Long
)

/**
 * Una vuelta. Los 3 sectores van como columnas: siempre son 3 y así las consultas
 * (por ejemplo, el mejor sector 1) quedan simples.
 */
@Entity(
    tableName = "lap",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class LapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val lapNumber: Int,
    val sector1Millis: Long,
    val sector2Millis: Long,
    val sector3Millis: Long
)
