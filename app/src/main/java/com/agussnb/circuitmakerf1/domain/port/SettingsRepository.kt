package com.agussnb.circuitmakerf1.domain.port

import com.agussnb.circuitmakerf1.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    fun update(transform: (AppSettings) -> AppSettings)
}
