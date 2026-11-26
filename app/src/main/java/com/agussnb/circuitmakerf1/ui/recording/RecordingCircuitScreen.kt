package com.agussnb.circuitmakerf1.ui.recording

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agussnb.circuitmakerf1.domain.service.MAX_ACCURACY_METERS
import com.agussnb.circuitmakerf1.domain.service.MIN_LOOP_DISTANCE_METERS
import com.agussnb.circuitmakerf1.ui.components.InfoCard
import com.agussnb.circuitmakerf1.ui.components.InfoRow
import com.agussnb.circuitmakerf1.ui.components.ScreenScaffold
import com.agussnb.circuitmakerf1.ui.components.TrackMap
import com.agussnb.circuitmakerf1.ui.components.formatKm
import com.agussnb.circuitmakerf1.ui.components.rememberLocationPermissionRequest
import com.agussnb.circuitmakerf1.ui.theme.TimingGreen
import com.agussnb.circuitmakerf1.ui.theme.TimingYellow

@Composable
fun RecordingCircuitScreen(
    onBack: () -> Unit,
    onTrackSaved: (Int) -> Unit,
    viewModel: RecordingViewModel = viewModel(factory = RecordingViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val requestPermissionAndStart = rememberLocationPermissionRequest(
        onGranted = viewModel::startRecording,
        onDenied = viewModel::onPermissionDenied
    )

    ScreenScaffold(title = "Crear circuito", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isSimulated) {
                Text(
                    "🛰️ GPS simulado activo (se cambia en Opciones)",
                    color = TimingYellow,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Altura fija porque la pantalla scrollea: un mapa dentro de un scroll necesita alto conocido.
            TrackMap(
                path = uiState.path,
                layout = uiState.preview,
                currentPosition = uiState.currentPosition,
                followPosition = uiState.isRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            if (viewModel.showPermissionWarning) {
                Text(
                    "Necesitás habilitar la ubicación precisa para grabar un circuito.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            StatusBanner(uiState)

            RecordingButtons(
                uiState = uiState,
                onStart = requestPermissionAndStart,
                onStop = viewModel::stopRecording,
                onDiscard = viewModel::discardRecording
            )

            InfoCard(title = "Grabación") {
                InfoRow("Distancia recorrida", formatKm(uiState.distanceKm, decimals = 2))
                InfoRow(
                    "Precisión del GPS",
                    uiState.accuracyMeters?.let { "± ${it.toInt()} m" } ?: "—",
                    valueColor = accuracyColor(uiState.accuracyMeters)
                )
                uiState.distanceToStartMeters?.let {
                    InfoRow("Distancia a la largada", "${it.toInt()} m")
                }
                if (uiState.discardedPoints > 0) {
                    InfoRow("Lecturas descartadas (imprecisas)", uiState.discardedPoints.toString())
                }
            }

            if (uiState.hasFinishedRecording) {
                SaveForm(viewModel = viewModel, uiState = uiState, onTrackSaved = onTrackSaved)
            }
        }
    }
}

@Composable
private fun StatusBanner(uiState: RecordingUiState) {
    val (text, color) = when {
        uiState.isLoopClosed && uiState.isRecording ->
            "🏁 ¡Circuito cerrado! Tocá \"Terminar\" para guardarlo." to TimingGreen
        uiState.isLoopClosed ->
            "🏁 Circuito cerrado. Ponele nombre y guardalo." to TimingGreen
        uiState.isRecording && uiState.distanceKm * 1000 < MIN_LOOP_DISTANCE_METERS ->
            "Grabando… Recorrí tu circuito y volvé al punto de partida." to MaterialTheme.colorScheme.onSurface
        uiState.isRecording ->
            "Grabando… Volvé a la largada para cerrar el circuito." to MaterialTheme.colorScheme.onSurface
        uiState.hasFinishedRecording ->
            "El recorrido no se cerró: podés guardarlo igual, pero la meta y la llegada no coinciden." to TimingYellow
        else ->
            "Tocá \"Iniciar grabación\" en el punto donde querés la largada." to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Text(text, modifier = Modifier.padding(16.dp), color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RecordingButtons(
    uiState: RecordingUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit
) {
    val buttonModifier = Modifier.height(56.dp)
    when {
        uiState.isRecording -> Button(
            onClick = onStop,
            modifier = buttonModifier.fillMaxWidth(),
            colors = if (uiState.isLoopClosed) {
                ButtonDefaults.buttonColors(containerColor = TimingGreen, contentColor = Color.Black)
            } else {
                ButtonDefaults.buttonColors()
            }
        ) {
            Text(if (uiState.isLoopClosed) "Terminar" else "Detener grabación", fontWeight = FontWeight.Bold)
        }

        uiState.hasFinishedRecording -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDiscard, modifier = buttonModifier.weight(1f)) {
                Text("Descartar")
            }
            OutlinedButton(onClick = onStart, modifier = buttonModifier.weight(1f)) {
                Text("Grabar de nuevo")
            }
        }

        else -> Button(onClick = onStart, modifier = buttonModifier.fillMaxWidth()) {
            Text("Iniciar grabación", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SaveForm(
    viewModel: RecordingViewModel,
    uiState: RecordingUiState,
    onTrackSaved: (Int) -> Unit
) {
    InfoCard(title = "Tu circuito") {
        InfoRow("Longitud", formatKm(uiState.circuitLengthKm))
        InfoRow("Vueltas (regla FIA)", uiState.laps.toString())
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = viewModel.trackName,
            onValueChange = viewModel::onTrackNameChange,
            label = { Text("Nombre del circuito") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.trackCountry,
            onValueChange = viewModel::onTrackCountryChange,
            label = { Text("País (opcional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { viewModel.saveTrack(onSaved = onTrackSaved) },
            enabled = viewModel.trackName.isNotBlank() && !viewModel.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(52.dp)
        ) {
            Text("Guardar circuito", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun accuracyColor(accuracy: Float?): Color = when {
    accuracy == null -> Color.Unspecified
    accuracy <= 8f -> TimingGreen
    accuracy <= MAX_ACCURACY_METERS -> TimingYellow
    else -> MaterialTheme.colorScheme.error
}
