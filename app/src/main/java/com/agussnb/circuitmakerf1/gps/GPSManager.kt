package com.agussnb.circuitmakerf1.gps

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.location.Priority
import android.content.pm.PackageManager


public class GPSManager(private val context: Context) {
    val locationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    var avisoPermiso by remember { mutableStateOf(false) }
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setWaitForAccurateLocation(true).build()

    val puntosRecorridos = mutableListOf<Location>()

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations){
                println("Latitud: ${location.latitude}, Longitud: ${location.longitude}")

                if (isRecording) {
                    val ultimoPunto = puntosRecorridos.lastOrNull()

                    if(ultimoPunto != null){
                        val distanciaEntrePuntos = ultimoPunto.distanceTo(location)
                        distanciaTotal += distanciaEntrePuntos
                    }
                    puntosRecorridos.add(location)
                }
            }
        }
    }

     var isRecording by mutableStateOf(false)
    var distanciaTotal by mutableDoubleStateOf(0.0)
    fun startLocationUpdates() {
        val fineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION

        if (checkSelfPermission(context, fineLocation) == PackageManager.PERMISSION_GRANTED) {
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
        puntosRecorridos.clear()
        distanciaTotal = 0.0
    }

    fun stopRecording(){
        isRecording = false
    }

}
