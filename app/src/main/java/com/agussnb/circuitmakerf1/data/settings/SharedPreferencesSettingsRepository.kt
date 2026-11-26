package com.agussnb.circuitmakerf1.data.settings

import android.content.Context
import androidx.core.content.edit
import com.agussnb.circuitmakerf1.domain.model.AppSettings
import com.agussnb.circuitmakerf1.domain.model.SportType
import com.agussnb.circuitmakerf1.domain.port.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Guarda las opciones en SharedPreferences (un archivo clave-valor de Android).
 * Para unas pocas opciones alcanza; si crecen, se puede migrar a DataStore sin tocar el dominio.
 */
class SharedPreferencesSettingsRepository(context: Context) : SettingsRepository {

    private val prefs = context.applicationContext
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        prefs.edit {
            putBoolean(KEY_SIMULATE_GPS, updated.simulateGps)
            putFloat(KEY_RACE_DISTANCE_KM, updated.raceDistanceKm.toFloat())
            putString(KEY_SPORT_TYPE, updated.sportType.name)
        }
    }

    private fun load(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            simulateGps = prefs.getBoolean(KEY_SIMULATE_GPS, defaults.simulateGps),
            raceDistanceKm = prefs.getFloat(KEY_RACE_DISTANCE_KM, defaults.raceDistanceKm.toFloat()).toDouble(),
            sportType = SportType.entries.firstOrNull {
                it.name == prefs.getString(KEY_SPORT_TYPE, null)
            } ?: defaults.sportType
        )
    }

    private companion object {
        const val KEY_SIMULATE_GPS = "simulate_gps"
        const val KEY_RACE_DISTANCE_KM = "race_distance_km"
        const val KEY_SPORT_TYPE = "sport_type"
    }
}
