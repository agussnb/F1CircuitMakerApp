package com.agussnb.circuitmakerf1.data.repository

import com.agussnb.circuitmakerf1.data.local.TrackDao
import com.agussnb.circuitmakerf1.data.local.toDomain
import com.agussnb.circuitmakerf1.data.local.toEntity
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTrackRepository(
    private val trackDao: TrackDao
) : TrackRepository {

    override val allTracks: Flow<List<Track>> =
        trackDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertTrack(track: Track) {
        trackDao.insert(track.toEntity())
    }

    override suspend fun deleteTrack(track: Track) {
        trackDao.delete(track.toEntity())
    }

    override suspend fun getTrackById(id: Int): Track? {
        return trackDao.getById(id)?.toDomain()
    }
}