package com.agussnb.circuitmakerf1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agussnb.circuitmakerf1.TrackMakerApp
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(repository: TrackRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.allTracks
        .map { tracks -> HomeUiState(tracks = tracks, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackMakerApp
                HomeViewModel(app.container.trackRepository)
            }
        }
    }
}