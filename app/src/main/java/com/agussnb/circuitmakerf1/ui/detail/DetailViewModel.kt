package com.agussnb.circuitmakerf1.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agussnb.circuitmakerf1.CircuitMakerApp
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Success(val track: Track) : DetailUiState
}

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TrackRepository
) : ViewModel() {

    private val trackId: Int = checkNotNull(savedStateHandle["trackId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val track = repository.getTrackById(trackId)
            _uiState.value = if (track != null) {
                DetailUiState.Success(track)
            } else {
                DetailUiState.NotFound
            }
        }
    }

    fun deleteTrack(onDeleted: () -> Unit) {
        val current = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            repository.deleteTrack(current.track)
            onDeleted()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CircuitMakerApp
                DetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = app.container.trackRepository
                )
            }
        }
    }
}