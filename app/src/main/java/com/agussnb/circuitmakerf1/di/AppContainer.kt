package com.agussnb.circuitmakerf1.di

import android.content.Context
import androidx.room.Room
import com.agussnb.circuitmakerf1.data.local.AppDatabase
import com.agussnb.circuitmakerf1.data.location.FusedLocationProvider
import com.agussnb.circuitmakerf1.data.location.GPSManager
import com.agussnb.circuitmakerf1.data.repository.RoomTrackRepository
import com.agussnb.circuitmakerf1.domain.port.LocationProvider
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.domain.service.TrackRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val locationProvider: LocationProvider by lazy {
        FusedLocationProvider(appContext)
    }

    val trackRecorder: TrackRecorder by lazy {
        TrackRecorder(locationProvider, applicationScope)
    }
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

}