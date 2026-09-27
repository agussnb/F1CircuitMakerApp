package com.agussnb.circuitmakerf1.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agussnb.circuitmakerf1.domain.model.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM track WHERE id = :id")
    suspend fun getById(id: Int): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: TrackEntity)

    @Delete
    suspend fun delete(track: TrackEntity)
}