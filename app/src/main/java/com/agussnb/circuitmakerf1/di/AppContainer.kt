package com.agussnb.circuitmakerf1.di

import android.content.Context
import androidx.room.Room
import com.agussnb.circuitmakerf1.data.local.AppDatabase
import com.agussnb.circuitmakerf1.data.local.MIGRATION_1_2
import com.agussnb.circuitmakerf1.data.location.FusedLocationProvider
import com.agussnb.circuitmakerf1.data.location.SimulatedLocationProvider
import com.agussnb.circuitmakerf1.data.location.SwitchableLocationProvider
import com.agussnb.circuitmakerf1.data.repository.RoomSessionRepository
import com.agussnb.circuitmakerf1.data.repository.RoomTrackRepository
import com.agussnb.circuitmakerf1.data.settings.SharedPreferencesSettingsRepository
import com.agussnb.circuitmakerf1.domain.port.LocationProvider
import com.agussnb.circuitmakerf1.domain.port.RouteSimulator
import com.agussnb.circuitmakerf1.domain.port.SessionRepository
import com.agussnb.circuitmakerf1.domain.port.SettingsRepository
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.domain.service.LapTimer
import com.agussnb.circuitmakerf1.domain.service.TrackRecorder
import com.agussnb.circuitmakerf1.service.RecordingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val settingsRepository: SettingsRepository by lazy {
        SharedPreferencesSettingsRepository(appContext)
    }

    private val simulatedLocationProvider by lazy { SimulatedLocationProvider() }

    val routeSimulator: RouteSimulator
        get() = simulatedLocationProvider

    private val locationProvider: LocationProvider by lazy {
        SwitchableLocationProvider(
            real = FusedLocationProvider(appContext),
            simulated = simulatedLocationProvider,
            settingsRepository = settingsRepository
        )
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "circuit_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val trackRepository: TrackRepository by lazy {
        RoomTrackRepository(database)
    }

    val sessionRepository: SessionRepository by lazy {
        RoomSessionRepository(database)
    }

    val trackRecorder: TrackRecorder by lazy {
        TrackRecorder(locationProvider, applicationScope)
    }

    val lapTimer: LapTimer by lazy {
        LapTimer(locationProvider, sessionRepository, applicationScope)
    }

    /**
     * Mientras se graba un circuito o hay una sesión de vuelta rápida, mantiene vivo el
     * Foreground Service (notificación fija) para que Android no corte el GPS con la
     * pantalla apagada. Cuando las dos cosas terminan, lo detiene.
     */
    fun startTrackingServiceSync() {
        applicationScope.launch {
            combine(trackRecorder.state, lapTimer.state) { recording, session ->
                when {
                    recording.isRecording -> "Grabando circuito"
                    session.isActive -> "Sesión de vuelta rápida en curso"
                    else -> null
                }
            }
                .distinctUntilChanged()
                .collect { message ->
                    if (message != null) {
                        RecordingService.start(appContext, message)
                    } else {
                        RecordingService.stop(appContext)
                    }
                }
        }
    }
}
