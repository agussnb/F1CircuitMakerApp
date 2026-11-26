package com.agussnb.circuitmakerf1.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agussnb.circuitmakerf1.domain.service.FIA_RACE_DISTANCE_KM
import com.agussnb.circuitmakerf1.domain.service.fiaLapCount
import com.agussnb.circuitmakerf1.ui.components.InfoCard
import com.agussnb.circuitmakerf1.ui.components.InfoRow
import com.agussnb.circuitmakerf1.ui.components.ScreenScaffold
import com.agussnb.circuitmakerf1.ui.components.TrackMap
import com.agussnb.circuitmakerf1.ui.components.emoji
import com.agussnb.circuitmakerf1.ui.components.formatKm
import com.agussnb.circuitmakerf1.ui.components.formatLapTime
import com.agussnb.circuitmakerf1.ui.components.label
import com.agussnb.circuitmakerf1.ui.theme.SectorColors
import com.agussnb.circuitmakerf1.ui.theme.TimingPurple

@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    onStartSession: (Int) -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    // rememberSaveable: el diálogo sigue abierto si rotás la pantalla.
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val title = (uiState as? DetailUiState.Success)?.track?.name ?: "Circuito"

    ScreenScaffold(
        title = title,
        onBack = onBack,
        actions = {
            if (uiState is DetailUiState.Success) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Borrar circuito")
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            DetailUiState.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            DetailUiState.NotFound -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Circuito no encontrado") }

            is DetailUiState.Success -> DetailContent(
                state = state,
                onStartSession = { onStartSession(state.track.id) },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Borrar circuito?") },
            text = { Text("Se borran también todas sus vueltas cronometradas. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTrack(onDeleted = onBack)
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState.Success,
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = state.track
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.layout != null) {
            TrackMap(
                layout = state.layout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            SectorLegend()
        } else {
            Card(Modifier.fillMaxWidth()) {
                Text(
                    "Este circuito se creó con una versión anterior y no tiene el trazado guardado. " +
                            "Para usar el modo vuelta rápida, grabalo de nuevo.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = onStartSession,
            enabled = state.layout != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Modo vuelta rápida", fontWeight = FontWeight.Bold)
        }

        InfoCard(title = "Circuito") {
            if (track.country.isNotBlank()) InfoRow("País", track.country)
            InfoRow("Deporte", "${track.sportType.emoji()} ${track.sportType.label()}")
            InfoRow("Longitud", formatKm(track.lengthKm))
            InfoRow("Vueltas (regla FIA, ${FIA_RACE_DISTANCE_KM.toInt()} km)", track.laps.toString())
            if (state.raceDistanceKm != FIA_RACE_DISTANCE_KM) {
                InfoRow(
                    "Vueltas (${state.raceDistanceKm.toInt()} km)",
                    fiaLapCount(track.lengthKm, state.raceDistanceKm).toString()
                )
            }
        }

        InfoCard(title = "Tus récords") {
            val bests = state.bests
            if (bests.totalLaps == 0) {
                Text(
                    "Todavía no cronometraste vueltas en este circuito.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                InfoRow("Mejor vuelta", formatLapTime(bests.bestLapMillis), TimingPurple, monospace = true)
                InfoRow("Vuelta ideal", formatLapTime(bests.idealLapMillis), monospace = true)
                bests.bestSectorsMillis.forEachIndexed { index, best ->
                    InfoRow("Mejor sector ${index + 1}", formatLapTime(best), TimingPurple, monospace = true)
                }
                InfoRow("Vueltas cronometradas", bests.totalLaps.toString())
            }
        }
    }
}

@Composable
private fun SectorLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SectorColors.forEachIndexed { index, color ->
            Text("● Sector ${index + 1}", color = color, fontWeight = FontWeight.Bold)
        }
    }
    Text(
        "La línea blanca es la meta.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
