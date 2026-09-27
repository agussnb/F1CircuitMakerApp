package com.agussnb.circuitmakerf1.ui.recording

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.agussnb.circuitmakerf1.data.location.GPSManager
import com.agussnb.circuitmakerf1.domain.model.Track
import kotlinx.coroutines.launch
import android.Manifest
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.ui.components.AddButton
import kotlin.math.floor


@Composable
fun RecordingCircuitScreen(gpsManager: GPSManager, repository: TrackRepository, onBack: () -> Unit) {
    var circuitName by remember { mutableStateOf("") }
    var circuitCountry by remember { mutableStateOf("") }
    var permitWarn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val kilometers : Double = gpsManager.totalDistance / 1000
    val distanceText = "%.2f".format(kilometers)
    val laps = if (kilometers > 0) floor(305.0 / kilometers).toInt() + 1 else 0
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineConcedido = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineConcedido) {
            permitWarn = false
            gpsManager.startLocationUpdates()
            gpsManager.startRecording()
        } else {
            permitWarn = true
        }
    }

    Column(modifier = Modifier.verticalScroll(scrollState)) {
        AddButton(onClick = onBack, label = "Volver", modifier = Modifier.padding(top = 20.dp))
        AddButton(
            onClick = {
                if (gpsManager.isRecording) {
                    gpsManager.stopRecording()
                    gpsManager.stopLocationUpdates()
                } else {
                    val tieneFine = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tieneFine) {
                        gpsManager.startLocationUpdates()
                        gpsManager.startRecording()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            },
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth(),
            label = if (gpsManager.isRecording) "Detener grabación" else "Iniciar grabación"
        )
        if (permitWarn) {
            Text(
                "Necesitás habilitar la ubicación precisa para grabar un circuito",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text("Distancia recorrida: $distanceText km")

        if (!gpsManager.isRecording && kilometers > 0.005) {
            Text("¡Circuito completado!", modifier = Modifier.padding(top = 16.dp))

            TextField(
                value = circuitName,
                onValueChange = { circuitName = it },
                label = { Text("Dale un nombre a tu circuito") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = circuitCountry,
                onValueChange = { circuitCountry = it },
                label = { Text("Pais de tu circuito") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            AddButton(
                onClick = {
                    scope.launch {
                        repository.insertsTrack(
                            Track(
                                name = circuitName,
                                country = circuitCountry,
                                lengthKm = kilometers,
                                laps = laps
                            )
                        )
                        gpsManager.totalDistance = 0.0
                        circuitName = ""
                        circuitCountry = ""
                    }
                },
                label = "Guardar en Base de Datos",
                modifier = Modifier.fillMaxWidth(),
                enabled = circuitName.isNotBlank()
            )
        }
    }
}