package com.agussnb.circuitmakerf1.ui.recording

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agussnb.circuitmakerf1.TrackMakerApp
import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.model.TrackLayout
import com.agussnb.circuitmakerf1.domain.port.RouteSimulator
import com.agussnb.circuitmakerf1.domain.port.SettingsRepository
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.domain.service.RecordingState
import com.agussnb.circuitmakerf1.domain.service.TrackRecorder
import com.agussnb.circuitmakerf1.domain.service.buildTrackLayout
import com.agussnb.circuitmakerf1.domain.service.fiaLapCount
import com.agussnb.circuitmakerf1.domain.service.simplifyPath
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecordingUiState(
    val isRecording: Boolean = false,
    val distanceKm: Double = 0.0,
    val path: List<GeoPoint> = emptyList(),
    val currentPosition: GeoPoint? = null,
    val accuracyMeters: Float? = null,
    val discardedPoints: Int = 0,
    val distanceToStartMeters: Double? = null,
    val isLoopClosed: Boolean = false,
    /** El circuito ya armado (sectores, meta), disponible apenas se cierra. */
    val preview: TrackLayout? = null,
    val simplifiedPath: List<GeoPoint> = emptyList(),
    val isSimulated: Boolean = false
) {
    val circuitLengthKm: Double
        get() = preview?.let { it.lengthMeters / 1000 } ?: distanceKm

    val laps: Int
        get() = fiaLapCount(circuitLengthKm)

    val hasFinishedRecording: Boolean
        get() = !isRecording && path.size >= 2 && distanceKm > 0.01
}

class RecordingViewModel(
    private val trackRecorder: TrackRecorder,
    private val repository: TrackRepository,
    private val settingsRepository: SettingsRepository,
    private val routeSimulator: RouteSimulator
) : ViewModel() {

    val uiState: StateFlow<RecordingUiState> = combine(
        trackRecorder.state,
        settingsRepository.settings
    ) { recording, settings ->
        recording.toUiState(isSimulated = settings.simulateGps)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = trackRecorder.state.value.toUiState(settingsRepository.settings.value.simulateGps)
    )

    var trackName by mutableStateOf("")
        private set

    var trackCountry by mutableStateOf("")
        private set

    var showPermissionWarning by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    fun onTrackNameChange(value: String) {
        trackName = value
    }

    fun onTrackCountryChange(value: String) {
        trackCountry = value
    }

    fun onPermissionDenied() {
        showPermissionWarning = true
    }

    fun startRecording() {
        showPermissionWarning = false
        routeSimulator.useRoute(null) // si el GPS simulado está activo, usa el recorrido de demo
        trackRecorder.start()
    }

    fun stopRecording() {
        trackRecorder.stop()
    }

    fun discardRecording() {
        trackRecorder.reset()
        trackName = ""
        trackCountry = ""
    }

    fun saveTrack(onSaved: (Int) -> Unit) {
        if (trackName.isBlank() || isSaving) return
        val state = uiState.value
        val path = state.simplifiedPath.ifEmpty { state.path }

        isSaving = true
        viewModelScope.launch {
            val id = repository.insertTrack(
                Track(
                    name = trackName.trim(),
                    country = trackCountry.trim(),
                    lengthKm = state.circuitLengthKm,
                    laps = state.laps,
                    sportType = settingsRepository.settings.value.sportType,
                    path = path
                )
            )
            trackRecorder.reset()
            trackName = ""
            trackCountry = ""
            isSaving = false
            onSaved(id)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackMakerApp
                RecordingViewModel(
                    trackRecorder = app.container.trackRecorder,
                    repository = app.container.trackRepository,
                    settingsRepository = app.container.settingsRepository,
                    routeSimulator = app.container.routeSimulator
                )
            }
        }
    }
}

private fun RecordingState.toUiState(isSimulated: Boolean): RecordingUiState {
    val trackPath = trackPath()
    // Simplificamos el trazado (Douglas-Peucker) y armamos los sectores apenas se cierra,
    // así el usuario ve el circuito final antes de guardarlo.
    val simplified = if (isLoopClosed || !isRecording) simplifyPath(trackPath) else emptyList()
    return RecordingUiState(
        isRecording = isRecording,
        distanceKm = totalDistanceMeters / 1000,
        path = points.map { it.position },
        currentPosition = points.lastOrNull()?.position,
        accuracyMeters = lastAccuracyMeters,
        discardedPoints = discardedPoints,
        distanceToStartMeters = distanceToStartMeters,
        isLoopClosed = isLoopClosed,
        preview = if (isLoopClosed) buildTrackLayout(simplified) else null,
        simplifiedPath = simplified,
        isSimulated = isSimulated
    )
}
