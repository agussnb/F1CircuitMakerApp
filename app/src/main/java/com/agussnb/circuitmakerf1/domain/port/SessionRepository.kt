package com.agussnb.circuitmakerf1.domain.port

import com.agussnb.circuitmakerf1.domain.model.Lap
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    /** Crea una sesión de vuelta rápida y devuelve su id. */
    suspend fun startSession(trackId: Int, startedAtMillis: Long): Long

    suspend fun saveLap(sessionId: Long, lap: Lap)

    suspend fun deleteSession(sessionId: Long)

    /** Todas las vueltas cronometradas en un circuito (de todas las sesiones). */
    suspend fun getLaps(trackId: Int): List<Lap>

    fun observeLaps(trackId: Int): Flow<List<Lap>>
}
