package com.agussnb.circuitmakerf1.data.local

import com.agussnb.circuitmakerf1.domain.model.Track


    fun TrackEntity.toDomain(): Track = Track(
        id = id,
        name = name,
        country = country,
        lengthKm = lengthKm,
        laps = laps
    )

    fun Track.toEntity(): TrackEntity = TrackEntity(
        id = id,
        name = name,
        country = country,
        lengthKm = lengthKm,
        laps = laps
    )
