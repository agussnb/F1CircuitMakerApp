package com.agussnb.circuitmakerf1.ui.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agussnb.circuitmakerf1.TrackMakerApp
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.model.TrackLayout
import com.agussnb.circuitmakerf1.domain.port.RouteSimulator
import com.agussnb.circuitmakerf1.domain.port.SettingsRepository
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.domain.service.LapTimer
import com.agussnb.circuitmakerf1.domain.service.LapTimingSnapshot
import com.agussnb.circuitmakerf1.domain.service.buildTrackLayout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class LapSessionUiState(
    val isLoading: Boolean = true,
    val track: Track? = null,
    val layout: TrackLayout? = null,
    /** Hay una sesión corriendo en ESTE circuito. */
    val isActive: Boolean = false,
    /** Hay una sesión corriendo en OTRO circuito (al iniciar esta, se termina aquella). */
    val otherSessionActive: Boolean = false,
    val timing: LapTimingSnapshot = LapTimingSnapshot(),
    val accuracyMeters: Float? = null,
    val isSimulated: Boolean = false
)

class LapSessionViewModel(
    savedStateHandle: SavedStateHandle,
    repository: TrackRepository,
    settingsRepository: SettingsRepository,
    private val lapTimer: LapTimer,
    private val routeSimulator: RouteSimulator
) : ViewModel() {

    private val trackId: Int = checkNotNull(savedStateHandle["trackId"])

    private val trackWithLayout = flow {
        val track = repository.getTrackById(trackId)
        emit(track to track?.let { buildTrackLayout(it.path) })
    }

    val uiState: StateFlow<LapSessionUiState> = combine(
        trackWithLayout,
        lapTimer.state,
        settingsRepository.settings
    ) { (track, layout), session, settings ->
        val isThisTrack = session.trackId == trackId
        LapSessionUiState(
            isLoading = false,
            track = track,
            layout = layout,
            isActive = session.isActive && isThisTrack,
            otherSessionActive = session.isActive && !isThisTrack,
            // Si la última sesión fue de este circuito, mostramos sus vueltas aunque ya haya terminado.
            timing = if (isThisTrack) session.timing else LapTimingSnapshot(),
            accuracyMeters = if (isThisTrack) session.lastAccuracyMeters else null,
            isSimulated = settings.simulateGps
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LapSessionUiState()
    )

    var showPermissionWarning by mutableStateOf(false)
        private set

    fun onPermissionDenied() {
        showPermissionWarning = true
    }

    fun startSession() {
        val state = uiState.value
        val track = state.track ?: return
        val layout = state.layout ?: return
        showPermissionWarning = false
        routeSimulator.useRoute(track.path) // si el GPS simulado está activo, "pedalea" este circuito
        lapTimer.start(track.id, layout)
    }

    fun stopSession() {
        lapTimer.stop()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackMakerApp
                LapSessionViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = app.container.trackRepository,
                    settingsRepository = app.container.settingsRepository,
                    lapTimer = app.container.lapTimer,
                    routeSimulator = app.container.routeSimulator
                )
            }
        }
    }
}
