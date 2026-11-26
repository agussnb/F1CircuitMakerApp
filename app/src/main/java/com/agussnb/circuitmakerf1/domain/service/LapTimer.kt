package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.TrackLayout
import com.agussnb.circuitmakerf1.domain.port.LocationProvider
import com.agussnb.circuitmakerf1.domain.port.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LapSessionState(
    val isActive: Boolean = false,
    val trackId: Int? = null,
    val timing: LapTimingSnapshot = LapTimingSnapshot(),
    val lastAccuracyMeters: Float? = null
)

/**
 * Sesión de vuelta rápida. Igual que [TrackRecorder], vive en el `AppContainer`: la sesión
 * sigue aunque salgas de la pantalla. Conecta el GPS con el [LapTimingEngine] y guarda
 * cada vuelta completa en el repositorio apenas termina (si la app se cierra, no se pierde).
 */
class LapTimer(
    private val locationProvider: LocationProvider,
    private val sessionRepository: SessionRepository,
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val _state = MutableStateFlow(LapSessionState())
    val state: StateFlow<LapSessionState> = _state.asStateFlow()

    private var sessionJob: Job? = null

    fun start(trackId: Int, layout: TrackLayout) {
        if (_state.value.isActive) stop()

        _state.value = LapSessionState(isActive = true, trackId = trackId)

        sessionJob = scope.launch {
            var sessionId: Long? = null
            var savedLaps = 0
            try {
                val historicLaps = sessionRepository.getLaps(trackId)
                val id = sessionRepository.startSession(trackId, clock())
                sessionId = id

                val engine = LapTimingEngine(layout, historicLaps)
                _state.update { it.copy(timing = engine.snapshot) }

                locationProvider.locationUpdates()
                    .catch { _state.update { it.copy(isActive = false) } }
                    .collect { point ->
                        val completedLap = engine.onPoint(point)
                        _state.update {
                            it.copy(timing = engine.snapshot, lastAccuracyMeters = point.accuracyMeters)
                        }
                        if (completedLap != null) {
                            // NonCancellable: si justo frenás la sesión, la vuelta se guarda igual.
                            withContext(NonCancellable) { sessionRepository.saveLap(id, completedLap) }
                            savedLaps++
                        }
                    }
            } finally {
                // `finally` corre también al cancelar el Job (cuando tocás "Terminar sesión").
                // Una sesión sin vueltas completas no aporta nada al historial.
                val id = sessionId
                if (id != null && savedLaps == 0) {
                    withContext(NonCancellable) { sessionRepository.deleteSession(id) }
                }
            }
        }
    }

    fun stop() {
        sessionJob?.cancel()
        sessionJob = null
        _state.update { it.copy(isActive = false) }
    }
}
