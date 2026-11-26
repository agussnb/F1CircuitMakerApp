package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GeoPoint
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
import kotlin.math.max

/** Lecturas con más error que esto se descartan. */
const val MAX_ACCURACY_METERS = 20f

/** Movimientos más chicos que esto se ignoran (evita sumar distancia estando quieto). */
const val MIN_MOVEMENT_METERS = 3.0

/** Volver a menos de esta distancia de la largada cierra el circuito. */
const val LOOP_CLOSE_RADIUS_METERS = 25.0

/** Distancia mínima recorrida antes de poder cerrar el circuito. */
const val MIN_LOOP_DISTANCE_METERS = 200.0

/** Hay que haberse alejado al menos esto de la largada (si no, "cerraría" apenas arrancás). */
const val MIN_DISTANCE_FROM_START_METERS = 50.0

data class RecordingState(
    val isRecording: Boolean = false,
    val points: List<GpsPoint> = emptyList(),
    val totalDistanceMeters: Double = 0.0,
    /** Precisión de la última lectura recibida (aunque se haya descartado). */
    val lastAccuracyMeters: Float? = null,
    val discardedPoints: Int = 0,
    val maxDistanceFromStartMeters: Double = 0.0,
    /** Índice del punto en el que se cerró el circuito, o `null` si todavía no se cerró. */
    val closingPointIndex: Int? = null
) {
    val isLoopClosed: Boolean
        get() = closingPointIndex != null

    val distanceToStartMeters: Double?
        get() = if (points.size >= 2) distanceMeters(points.first(), points.last()) else null

    /** El trazado del circuito: hasta el punto de cierre si se cerró, o todo lo grabado si no. */
    fun trackPath(): List<GeoPoint> {
        val lastIndex = closingPointIndex ?: points.lastIndex
        return points.take(lastIndex + 1).map { it.position }
    }
}

/**
 * Agrega una lectura al estado aplicando los filtros de calidad y la detección de cierre.
 * Es una función pura: no toca el GPS ni corrutinas, así que se testea directo.
 */
fun RecordingState.withPoint(point: GpsPoint): RecordingState {
    val updated = copy(lastAccuracyMeters = point.accuracyMeters)

    if (point.accuracyMeters > MAX_ACCURACY_METERS) {
        return updated.copy(discardedPoints = discardedPoints + 1)
    }

    val last = points.lastOrNull() ?: return updated.copy(points = listOf(point))

    val step = distanceMeters(last, point)
    if (step < MIN_MOVEMENT_METERS) return updated

    val newPoints = points + point
    val total = totalDistanceMeters + step
    val fromStart = distanceMeters(points.first(), point)
    val maxFromStart = max(maxDistanceFromStartMeters, fromStart)

    val closesNow = closingPointIndex == null &&
            total >= MIN_LOOP_DISTANCE_METERS &&
            maxFromStart >= MIN_DISTANCE_FROM_START_METERS &&
            fromStart <= LOOP_CLOSE_RADIUS_METERS

    return updated.copy(
        points = newPoints,
        totalDistanceMeters = total,
        maxDistanceFromStartMeters = maxFromStart,
        closingPointIndex = if (closesNow) newPoints.lastIndex else closingPointIndex
    )
}

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
                .collect { point -> _state.update { it.withPoint(point) } }
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
}
