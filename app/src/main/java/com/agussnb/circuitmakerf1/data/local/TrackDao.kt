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
    fun getAll(): Flow<List<Track>>

    @Query(value = "SELECT * FROM track WHERE id = :id")
    suspend fun getById(id: Int): Track?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(track: Track)

    @Delete()
    suspend fun delete(track: Track)
}