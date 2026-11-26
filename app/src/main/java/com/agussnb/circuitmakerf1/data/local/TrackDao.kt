package com.agussnb.circuitmakerf1.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM track ORDER BY id DESC")
    fun getAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM track WHERE id = :id")
    suspend fun getById(id: Int): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: TrackEntity): Long

    @Query("DELETE FROM track WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM track_point WHERE trackId = :trackId ORDER BY position")
    suspend fun getPoints(trackId: Int): List<TrackPointEntity>

    @Insert
    suspend fun insertPoints(points: List<TrackPointEntity>)

    @Query("DELETE FROM track_point WHERE trackId = :trackId")
    suspend fun deletePoints(trackId: Int)
}
