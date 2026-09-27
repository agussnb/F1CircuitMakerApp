package com.agussnb.circuitmakerf1.ui.recording

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agussnb.circuitmakerf1.CircuitMakerApp
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.domain.service.TrackRecorder
import com.agussnb.circuitmakerf1.domain.service.fiaLapCount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecordingUiState(
    val isRecording: Boolean = false,
    val distanceKm: Double = 0.0,
    val laps: Int = 0
) {
    val hasFinishedRecording: Boolean
        get() = !isRecording && distanceKm > 0.005
}

class RecordingViewModel(
    private val trackRecorder: TrackRecorder,
    private val repository: TrackRepository
) : ViewModel() {

    val uiState: StateFlow<RecordingUiState> = trackRecorder.state
        .map { recording ->
            val km = recording.totalDistanceMeters / 1000
            RecordingUiState(
                isRecording = recording.isRecording,
                distanceKm = km,
                laps = fiaLapCount(km)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecordingUiState()
        )

    var trackName by mutableStateOf("")
        private set

    var trackCountry by mutableStateOf("")
        private set

    var showPermissionWarning by mutableStateOf(false)
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
        trackRecorder.start()
    }

    fun stopRecording() {
        trackRecorder.stop()
    }

    fun saveTrack() {
        if (trackName.isBlank()) return
        val state = uiState.value

        viewModelScope.launch {
            repository.insertTrack(
                Track(
                    name = trackName.trim(),
                    country = trackCountry.trim(),
                    lengthKm = state.distanceKm,
                    laps = state.laps
                )
            )
            trackRecorder.reset()
            trackName = ""
            trackCountry = ""
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CircuitMakerApp
                RecordingViewModel(
                    trackRecorder = app.container.trackRecorder,
                    repository = app.container.trackRepository
                )
            }
        }
    }
}