package com.agussnb.circuitmakerf1.data.repository

import androidx.room.withTransaction
import com.agussnb.circuitmakerf1.data.local.AppDatabase
import com.agussnb.circuitmakerf1.data.local.toDomain
import com.agussnb.circuitmakerf1.data.local.toEntity
import com.agussnb.circuitmakerf1.data.local.toPointEntities
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTrackRepository(
    private val database: AppDatabase
) : TrackRepository {

    private val trackDao = database.trackDao()
    private val sessionDao = database.sessionDao()

    override val allTracks: Flow<List<Track>> =
        trackDao.getAll().map { entities -> entities.map { it.toDomain() } }

    /**
     * `withTransaction`: o se guarda todo (circuito + puntos) o no se guarda nada.
     * Es el equivalente a `@Transactional` de Spring.
     */
    override suspend fun insertTrack(track: Track): Int = database.withTransaction {
        val id = trackDao.insert(track.toEntity()).toInt()
        trackDao.insertPoints(track.path.toPointEntities(id))
        id
    }

    override suspend fun deleteTrack(track: Track) {
        database.withTransaction {
            sessionDao.deleteLapsOfTrack(track.id)
            sessionDao.deleteSessionsOfTrack(track.id)
            trackDao.deletePoints(track.id)
            trackDao.deleteById(track.id)
        }
    }

    override suspend fun getTrackById(id: Int): Track? {
        val entity = trackDao.getById(id) ?: return null
        val path = trackDao.getPoints(id).map { it.toDomain() }
        return entity.toDomain(path)
    }
}
