package com.example.circuitmakerf1.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.circuitmakerf1.model.Circuit
import kotlinx.coroutines.flow.Flow

@Dao
interface CircuitDao {
    @Query("SELECT * FROM circuit")
    fun getAll(): Flow<List<Circuit>>

    @Query(value = "SELECT * FROM circuit WHERE id = :id")
    suspend fun getById(id: Int): Circuit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(circuit: Circuit)

    @Delete()
    suspend fun delete(circuit: Circuit)
}