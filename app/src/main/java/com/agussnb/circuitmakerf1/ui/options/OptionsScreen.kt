package com.agussnb.circuitmakerf1.ui.options

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agussnb.circuitmakerf1.domain.model.SportType
import com.agussnb.circuitmakerf1.domain.service.FIA_RACE_DISTANCE_KM
import com.agussnb.circuitmakerf1.ui.components.InfoCard
import com.agussnb.circuitmakerf1.ui.components.ScreenScaffold
import com.agussnb.circuitmakerf1.ui.components.emoji
import com.agussnb.circuitmakerf1.ui.components.label

private val RACE_DISTANCE_OPTIONS = listOf(FIA_RACE_DISTANCE_KM, 200.0, 100.0, 50.0, 20.0)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionsScreen(
    onBack: () -> Unit,
    viewModel: OptionsViewModel = viewModel(factory = OptionsViewModel.Factory)
) {
    val settings by viewModel.settings.collectAsState()

    ScreenScaffold(title = "Opciones", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(title = "Pruebas") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GPS simulado", fontWeight = FontWeight.Bold)
                        Text(
                            "La app recorre un circuito sola, sin que te muevas. Al crear un circuito usa " +
                                    "un recorrido de demo en Palermo; en vuelta rápida da vueltas sobre el circuito elegido.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.simulateGps,
                        onCheckedChange = viewModel::setSimulateGps,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            InfoCard(title = "Distancia de carrera") {
                Text(
                    "Se usa para calcular cuántas vueltas tendría una carrera en tus circuitos. " +
                            "La regla de la FIA es 305 km.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RACE_DISTANCE_OPTIONS.forEach { km ->
                        FilterChip(
                            selected = settings.raceDistanceKm == km,
                            onClick = { viewModel.setRaceDistance(km) },
                            label = {
                                Text(if (km == FIA_RACE_DISTANCE_KM) "${km.toInt()} km (FIA)" else "${km.toInt()} km")
                            }
                        )
                    }
                }
            }

            InfoCard(title = "Deporte") {
                Text(
                    "Deporte de los circuitos que crees a partir de ahora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SportType.entries.forEach { sport ->
                        FilterChip(
                            selected = settings.sportType == sport,
                            onClick = { viewModel.setSportType(sport) },
                            label = { Text("${sport.emoji()} ${sport.label()}") }
                        )
                    }
                }
            }
        }
    }
}
