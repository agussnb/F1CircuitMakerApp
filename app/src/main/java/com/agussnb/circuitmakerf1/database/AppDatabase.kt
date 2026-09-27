package com.agussnb.circuitmakerf1.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agussnb.circuitmakerf1.dao.CircuitDao
import com.agussnb.circuitmakerf1.model.Circuit

@Database(entities = [Circuit::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun circuitDao(): CircuitDao
}

