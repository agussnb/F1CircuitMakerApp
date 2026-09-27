package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import com.agussnb.circuitmakerf1.domain.port.LocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordingState(
    val isRecording: Boolean = false,
    val points: List<GpsPoint> = emptyList(),
    val totalDistanceMeters: Double = 0.0
)

class TrackRecorder(
    private val locationProvider: LocationProvider,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(RecordingState())
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    private var recordingJob: Job? = null

    fun start() {
        if (_state.value.isRecording) return

        _state.value = RecordingState(isRecording = true)

        recordingJob = scope.launch {
            locationProvider.locationUpdates()
                .catch { _state.update { it.copy(isRecording = false) } }
                .collect { point -> addPoint(point) }
        }
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        _state.update { it.copy(isRecording = false) }
    }

    fun reset() {
        stop()
        _state.value = RecordingState()
    }

    private fun addPoint(point: GpsPoint) {
        _state.update { current ->
            val lastPoint = current.points.lastOrNull()
            val addedDistance = if (lastPoint != null) distanceMeters(lastPoint, point) else 0.0

            current.copy(
                points = current.points + point,
                totalDistanceMeters = current.totalDistanceMeters + addedDistance
            )
        }
    }
}

