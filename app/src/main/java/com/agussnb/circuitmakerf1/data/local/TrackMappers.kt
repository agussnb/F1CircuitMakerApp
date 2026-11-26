package com.agussnb.circuitmakerf1.data.local

import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import com.agussnb.circuitmakerf1.domain.model.Lap
import com.agussnb.circuitmakerf1.domain.model.SportType
import com.agussnb.circuitmakerf1.domain.model.Track

fun TrackEntity.toDomain(path: List<GeoPoint> = emptyList()): Track = Track(
    id = id,
    name = name,
    country = country,
    lengthKm = lengthKm,
    laps = laps,
    sportType = SportType.entries.firstOrNull { it.name == sportType } ?: SportType.CYCLING,
    path = path
)

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    name = name,
    country = country,
    lengthKm = lengthKm,
    laps = laps,
    sportType = sportType.name
)

fun TrackPointEntity.toDomain(): GeoPoint = GeoPoint(latitude, longitude)

fun List<GeoPoint>.toPointEntities(trackId: Int): List<TrackPointEntity> =
    mapIndexed { index, point ->
        TrackPointEntity(
            trackId = trackId,
            position = index,
            latitude = point.latitude,
            longitude = point.longitude
        )
    }

fun LapEntity.toDomain(): Lap = Lap(
    number = lapNumber,
    sectorTimesMillis = listOf(sector1Millis, sector2Millis, sector3Millis)
)

fun Lap.toEntity(sessionId: Long): LapEntity = LapEntity(
    sessionId = sessionId,
    lapNumber = number,
    sector1Millis = sectorTimesMillis[0],
    sector2Millis = sectorTimesMillis[1],
    sector3Millis = sectorTimesMillis[2]
)
