package com.agussnb.circuitmakerf1.domain.port
import kotlinx.coroutines.flow.Flow
import com.agussnb.circuitmakerf1.domain.model.Track

interface TrackRepository {
    val allTracks: Flow<List<Track>>
    suspend fun insertTrack(track: Track)
    suspend fun deleteTrack(track: Track)
    suspend fun getTrackById(id: Int): Track?
}