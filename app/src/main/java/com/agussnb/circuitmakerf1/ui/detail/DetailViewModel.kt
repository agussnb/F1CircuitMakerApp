package com.agussnb.circuitmakerf1.ui.detail

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
import com.agussnb.circuitmakerf1.domain.port.SessionRepository
import com.agussnb.circuitmakerf1.domain.port.SettingsRepository
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.domain.service.PersonalBests
import com.agussnb.circuitmakerf1.domain.service.buildTrackLayout
import com.agussnb.circuitmakerf1.domain.service.personalBests
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Success(
        val track: Track,
        /** `null` si el circuito no tiene trazado guardado (creado con una versión vieja). */
        val layout: TrackLayout?,
        val bests: PersonalBests,
        val raceDistanceKm: Double
    ) : DetailUiState
}

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TrackRepository,
    sessionRepository: SessionRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val trackId: Int = checkNotNull(savedStateHandle["trackId"])

    /**
     * `combine` junta dos flujos: el circuito (se carga una vez) y sus vueltas (se actualiza
     * solo cuando terminás una sesión). Cada vez que cualquiera cambia, se arma un estado nuevo.
     */
    val uiState: StateFlow<DetailUiState> = combine(
        flow { emit(repository.getTrackById(trackId)) },
        sessionRepository.observeLaps(trackId)
    ) { track, laps ->
        if (track == null) {
            DetailUiState.NotFound
        } else {
            DetailUiState.Success(
                track = track,
                layout = buildTrackLayout(track.path),
                bests = personalBests(laps),
                raceDistanceKm = settingsRepository.settings.value.raceDistanceKm
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailUiState.Loading
    )

    fun deleteTrack(onDeleted: () -> Unit) {
        val current = uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            repository.deleteTrack(current.track)
            onDeleted()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackMakerApp
                DetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = app.container.trackRepository,
                    sessionRepository = app.container.sessionRepository,
                    settingsRepository = app.container.settingsRepository
                )
            }
        }
    }
}
