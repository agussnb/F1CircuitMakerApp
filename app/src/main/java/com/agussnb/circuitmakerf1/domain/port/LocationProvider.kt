package com.agussnb.circuitmakerf1.domain.port

import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    fun locationUpdates(): Flow<GpsPoint>
}