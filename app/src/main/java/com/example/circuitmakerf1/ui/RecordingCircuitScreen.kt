package com.example.circuitmakerf1.ui

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
import com.example.circuitmakerf1.data.CircuitRepository
import com.example.circuitmakerf1.gps.GPSManager
import com.example.circuitmakerf1.model.Circuit
import kotlinx.coroutines.launch
import android.Manifest
import kotlin.math.floor


@Composable
fun RecordingCircuitScreen(gpsManager: GPSManager, repository: CircuitRepository, onBack: () -> Unit) {
    var nombreCircuito by remember { mutableStateOf("") }
    var paisCircuito by remember { mutableStateOf("") }
    var avisoPermiso by remember { mutableStateOf(false) }   // 🆕 estado del aviso
    val scope = rememberCoroutineScope()
    val kilometros = gpsManager.distanciaTotal / 1000
    val textoDistancia = "%.2f".format(kilometros)
    val vueltas = if (kilometros > 0) floor(305.0 / kilometros).toInt() + 1 else 0
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineConcedido = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineConcedido) {
            avisoPermiso = false
            gpsManager.startLocationUpdates()
            gpsManager.startRecording()
        } else {
            avisoPermiso = true
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
        if (avisoPermiso) {
            Text(
                "Necesitás habilitar la ubicación precisa para grabar un circuito",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text("Distancia recorrida: $textoDistancia km")

        if (!gpsManager.isRecording && kilometros > 0.005) {
            Text("¡Circuito completado!", modifier = Modifier.padding(top = 16.dp))

            TextField(
                value = nombreCircuito,
                onValueChange = { nombreCircuito = it },
                label = { Text("Dale un nombre a tu circuito") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = paisCircuito,
                onValueChange = { paisCircuito = it },
                label = { Text("Pais de tu circuito") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            AddButton(
                onClick = {
                    scope.launch {
                        repository.insertsCircuit(
                            Circuit(
                                name = nombreCircuito,
                                country = paisCircuito,
                                lengthKm = kilometros,
                                laps = vueltas.toInt()
                            )
                        )
                        gpsManager.distanciaTotal = 0.0
                        nombreCircuito = ""
                    }

                },
                label = "Guardar en Base de Datos",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}