package com.agussnb.circuitmakerf1.data.repository

import com.agussnb.circuitmakerf1.data.local.TrackDao
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import kotlinx.coroutines.flow.Flow

class RoomTrackRepository(
    private val trackDao: TrackDao
) : TrackRepository {

    override val allTracks: Flow<List<Track>> = trackDao.getAll()

    override suspend fun insertTrack(track: Track) {
        trackDao.insert(track)
    }

    override suspend fun deleteTrack(track: Track) {
        trackDao.delete(track)
    }

    override suspend fun getTrackById(id: Int): Track? {
        return trackDao.getById(id)
    }
}