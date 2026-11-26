package com.agussnb.circuitmakerf1.ui.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agussnb.circuitmakerf1.TrackMakerApp
import com.agussnb.circuitmakerf1.domain.model.AppSettings
import com.agussnb.circuitmakerf1.domain.model.SportType
import com.agussnb.circuitmakerf1.domain.port.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class OptionsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun setSimulateGps(enabled: Boolean) {
        settingsRepository.update { it.copy(simulateGps = enabled) }
    }

    fun setRaceDistance(km: Double) {
        settingsRepository.update { it.copy(raceDistanceKm = km) }
    }

    fun setSportType(sportType: SportType) {
        settingsRepository.update { it.copy(sportType = sportType) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackMakerApp
                OptionsViewModel(app.container.settingsRepository)
            }
        }
    }
}
