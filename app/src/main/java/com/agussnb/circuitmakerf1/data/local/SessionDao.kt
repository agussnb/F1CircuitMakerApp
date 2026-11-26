package com.agussnb.circuitmakerf1.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Insert
    suspend fun insertLap(lap: LapEntity)

    @Query("DELETE FROM lap WHERE sessionId = :sessionId")
    suspend fun deleteLapsOfSession(sessionId: Long)

    @Query("DELETE FROM session WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM lap WHERE sessionId IN (SELECT id FROM session WHERE trackId = :trackId)")
    suspend fun deleteLapsOfTrack(trackId: Int)

    @Query("DELETE FROM session WHERE trackId = :trackId")
    suspend fun deleteSessionsOfTrack(trackId: Int)

    @Query(
        "SELECT lap.* FROM lap INNER JOIN session ON lap.sessionId = session.id " +
                "WHERE session.trackId = :trackId ORDER BY lap.id"
    )
    suspend fun getLapsForTrack(trackId: Int): List<LapEntity>

    @Query(
        "SELECT lap.* FROM lap INNER JOIN session ON lap.sessionId = session.id " +
                "WHERE session.trackId = :trackId ORDER BY lap.id"
    )
    fun observeLapsForTrack(trackId: Int): Flow<List<LapEntity>>
}
