package com.agussnb.circuitmakerf1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agussnb.circuitmakerf1.domain.model.Track

@Database(entities = [Track::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}