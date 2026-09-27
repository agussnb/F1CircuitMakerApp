package com.agussnb.circuitmakerf1.ui.recording

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agussnb.circuitmakerf1.ui.components.AddButton
import com.agussnb.circuitmakerf1.ui.components.TrackMap


@Composable
fun RecordingCircuitScreen(
    onBack: () -> Unit,
    viewModel: RecordingViewModel = viewModel(factory = RecordingViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineGranted) {
            viewModel.startRecording()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    Column(modifier = Modifier.verticalScroll(scrollState)) {
        AddButton(onClick = onBack, label = "Volver", modifier = Modifier.padding(top = 20.dp))

        AddButton(
            onClick = {
                if (uiState.isRecording) {
                    viewModel.stopRecording()
                } else {
                    val hasFinePermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasFinePermission) {
                        viewModel.startRecording()
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
            label = if (uiState.isRecording) "Detener grabación" else "Iniciar grabación"
        )

        TrackMap(
            points = uiState.points,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(vertical = 8.dp)
        )

        if (viewModel.showPermissionWarning) {
            Text(
                "Necesitás habilitar la ubicación precisa para grabar un circuito",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text("Distancia recorrida: ${"%.2f".format(uiState.distanceKm)} km")

        if (uiState.hasFinishedRecording) {
            Text("¡Circuito completado!", modifier = Modifier.padding(top = 16.dp))

            TextField(
                value = viewModel.trackName,
                onValueChange = viewModel::onTrackNameChange,
                label = { Text("Dale un nombre a tu circuito") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = viewModel.trackCountry,
                onValueChange = viewModel::onTrackCountryChange,
                label = { Text("País de tu circuito") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            AddButton(
                onClick = viewModel::saveTrack,
                label = "Guardar en Base de Datos",
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.trackName.isNotBlank()
            )
        }
    }
}