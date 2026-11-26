package com.agussnb.circuitmakerf1.data.location

import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import com.agussnb.circuitmakerf1.domain.port.LocationProvider
import com.agussnb.circuitmakerf1.domain.port.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Elige entre el GPS real y el simulado según las opciones, en el momento en que empieza
 * la grabación o la sesión. El dominio no se entera: para él es un `LocationProvider` más.
 */
class SwitchableLocationProvider(
    private val real: LocationProvider,
    private val simulated: LocationProvider,
    private val settingsRepository: SettingsRepository
) : LocationProvider {

    override fun locationUpdates(): Flow<GpsPoint> = flow {
        val source = if (settingsRepository.settings.value.simulateGps) simulated else real
        emitAll(source.locationUpdates())
    }
}
