package com.agussnb.circuitmakerf1.data.repository

import androidx.room.withTransaction
import com.agussnb.circuitmakerf1.data.local.AppDatabase
import com.agussnb.circuitmakerf1.data.local.SessionEntity
import com.agussnb.circuitmakerf1.data.local.toDomain
import com.agussnb.circuitmakerf1.data.local.toEntity
import com.agussnb.circuitmakerf1.domain.model.Lap
import com.agussnb.circuitmakerf1.domain.port.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSessionRepository(
    private val database: AppDatabase
) : SessionRepository {

    private val sessionDao = database.sessionDao()

    override suspend fun startSession(trackId: Int, startedAtMillis: Long): Long =
        sessionDao.insertSession(SessionEntity(trackId = trackId, startedAtMillis = startedAtMillis))

    override suspend fun saveLap(sessionId: Long, lap: Lap) {
        sessionDao.insertLap(lap.toEntity(sessionId))
    }

    override suspend fun deleteSession(sessionId: Long) {
        database.withTransaction {
            sessionDao.deleteLapsOfSession(sessionId)
            sessionDao.deleteSession(sessionId)
        }
    }

    override suspend fun getLaps(trackId: Int): List<Lap> =
        sessionDao.getLapsForTrack(trackId).map { it.toDomain() }

    override fun observeLaps(trackId: Int): Flow<List<Lap>> =
        sessionDao.observeLapsForTrack(trackId).map { laps -> laps.map { it.toDomain() } }
}
