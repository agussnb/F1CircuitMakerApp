package com.agussnb.circuitmakerf1.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

public class GPSManager(private val context: Context) {
    val locationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setWaitForAccurateLocation(true).build()

    val recordedPoints = mutableListOf<Location>()

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations){
                println("Latitud: ${location.latitude}, Longitud: ${location.longitude}")

                if (isRecording) {
                    val lastPoint = recordedPoints.lastOrNull()

                    if(lastPoint != null){
                        val distanceBetweenPoints = lastPoint.distanceTo(location)
                        totalDistance += distanceBetweenPoints
                    }
                    recordedPoints.add(location)
                }
            }
        }
    }

     var isRecording by mutableStateOf(false)
    var totalDistance by mutableDoubleStateOf(0.0)
    fun startLocationUpdates() {
        val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(context, fineLocation) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            println("No hay permisos de ubicación")
        }
    }

    fun stopLocationUpdates(){
        locationClient.removeLocationUpdates(locationCallback)
        isRecording = false
    }

    fun startRecording(){
        isRecording = true
        recordedPoints.clear()
        totalDistance = 0.0
    }

    fun stopRecording(){
        isRecording = false
    }

}