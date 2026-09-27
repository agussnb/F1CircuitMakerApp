package com.agussnb.circuitmakerf1.di

import android.content.Context
import androidx.room.Room
import com.agussnb.circuitmakerf1.data.local.AppDatabase
import com.agussnb.circuitmakerf1.data.location.GPSManager
import com.agussnb.circuitmakerf1.data.repository.RoomTrackRepository
import com.agussnb.circuitmakerf1.domain.port.TrackRepository

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "circuit_database"
        ).build()
    }

    val trackRepository: TrackRepository by lazy {
        RoomTrackRepository(database.trackDao())
    }

    val gpsManager: GPSManager by lazy {
        GPSManager(appContext)
    }
}