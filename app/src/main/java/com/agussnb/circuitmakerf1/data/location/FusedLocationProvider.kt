package com.agussnb.circuitmakerf1.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import com.agussnb.circuitmakerf1.domain.port.LocationProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FusedLocationProvider(context: Context) : LocationProvider {

    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)
    private val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setWaitForAccurateLocation(true)
        .build()

    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<GpsPoint> = callbackFlow {
        val hasPermission = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            close(SecurityException("Falta el permiso de ubicación precisa"))
            return@callbackFlow
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    trySend(location.toGpsPoint())
                }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }
}

private fun Location.toGpsPoint(): GpsPoint = GpsPoint(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracy,
    timestampMillis = time
)