package com.agussnb.circuitmakerf1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackEntity::class,
        TrackPointEntity::class,
        SessionEntity::class,
        LapEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun sessionDao(): SessionDao
}
