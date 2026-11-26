package com.agussnb.circuitmakerf1.ui.session

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agussnb.circuitmakerf1.domain.service.CompletedLap
import com.agussnb.circuitmakerf1.domain.service.LapTimingSnapshot
import com.agussnb.circuitmakerf1.domain.service.SECTOR_COUNT
import com.agussnb.circuitmakerf1.domain.service.TimingPhase
import com.agussnb.circuitmakerf1.ui.components.ScreenScaffold
import com.agussnb.circuitmakerf1.ui.components.TrackMap
import com.agussnb.circuitmakerf1.ui.components.formatDelta
import com.agussnb.circuitmakerf1.ui.components.formatLapTime
import com.agussnb.circuitmakerf1.ui.components.rememberLocationPermissionRequest
import com.agussnb.circuitmakerf1.ui.theme.SectorColors
import com.agussnb.circuitmakerf1.ui.theme.TimingGreen
import com.agussnb.circuitmakerf1.ui.theme.TimingPurple
import com.agussnb.circuitmakerf1.ui.theme.TimingYellow
import com.agussnb.circuitmakerf1.ui.theme.toColor
import kotlinx.coroutines.delay

@Composable
fun LapSessionScreen(
    onBack: () -> Unit,
    viewModel: LapSessionViewModel = viewModel(factory = LapSessionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val requestPermissionAndStart = rememberLocationPermissionRequest(
        onGranted = viewModel::startSession,
        onDenied = viewModel::onPermissionDenied
    )

    ScreenScaffold(title = uiState.track?.name ?: "Vuelta rápida", onBack = onBack) { padding ->
        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@ScreenScaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isSimulated) {
                Text(
                    "🛰️ GPS simulado activo",
                    color = TimingYellow,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TrackMap(
                layout = uiState.layout,
                currentPosition = uiState.timing.position,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            TimerPanel(uiState.timing, isActive = uiState.isActive)

            if (viewModel.showPermissionWarning) {
                Text(
                    "Necesitás habilitar la ubicación precisa para cronometrar.",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (uiState.otherSessionActive) {
                Text(
                    "Hay una sesión en curso en otro circuito. Si empezás acá, esa se termina.",
                    color = TimingYellow,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.isActive) {
                Button(
                    onClick = viewModel::stopSession,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Terminar sesión", fontWeight = FontWeight.Bold) }
            } else {
                Button(
                    onClick = requestPermissionAndStart,
                    enabled = uiState.layout != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) { Text("Iniciar sesión", fontWeight = FontWeight.Bold) }
            }

            LapList(
                laps = uiState.timing.completedLaps,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Hora actual en el mismo reloj que las lecturas GPS (`elapsedRealtime`), actualizada
 * ~30 veces por segundo mientras [running] sea `true`. Con esto el cronómetro "corre" en
 * pantalla entre lectura y lectura del GPS.
 */
@Composable
private fun rememberClock(running: Boolean): State<Long> =
    produceState(initialValue = SystemClock.elapsedRealtime(), running) {
        while (running) {
            value = SystemClock.elapsedRealtime()
            delay(33)
        }
    }

@Composable
private fun TimerPanel(timing: LapTimingSnapshot, isActive: Boolean) {
    val running = isActive && timing.phase == TimingPhase.RUNNING
    val now by rememberClock(running)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val header = when {
                !isActive && timing.completedLaps.isEmpty() -> "Tocá \"Iniciar sesión\" y cruzá la línea de meta"
                !isActive -> "Sesión terminada"
                timing.phase == TimingPhase.WAITING_FOR_START -> "Vuelta de salida · cruzá la meta para empezar"
                else -> "Vuelta ${timing.completedLaps.size + 1} · Sector ${timing.currentSectorIndex + 1}"
            }
            Text(
                header,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            val liveTime = if (running) timing.lapStartMillis?.let { now - it } else null
            val bigTime = liveTime ?: timing.completedLaps.lastOrNull()?.lapTimeMillis
            Text(
                text = formatLapTime(bigTime),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            timing.lastDeltaMillis?.let { delta ->
                Text(
                    text = "${formatDelta(delta)}  vs mejor vuelta",
                    color = if (delta <= 0) TimingGreen else MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mientras se corre, mostramos los sectores de la vuelta actual; si no, los de la última.
                val sectors = if (running) timing.currentLapSectors
                else timing.completedLaps.lastOrNull()?.sectors.orEmpty()

                repeat(SECTOR_COUNT) { index ->
                    val result = sectors.getOrNull(index)
                    val isCurrent = running && index == timing.currentSectorIndex
                    val liveSector = if (isCurrent) timing.sectorStartMillis?.let { now - it } else null
                    SectorTile(
                        label = "S${index + 1}",
                        time = result?.timeMillis ?: liveSector,
                        color = result?.color?.toColor(),
                        highlight = isCurrent,
                        sectorColor = SectorColors[index],
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mejor vuelta", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    formatLapTime(timing.bestLapMillis),
                    color = TimingPurple,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SectorTile(
    label: String,
    time: Long?,
    color: Color?,
    highlight: Boolean,
    sectorColor: Color,
    modifier: Modifier = Modifier
) {
    val background = color ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = if (color != null) Color.Black else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            color = if (color != null) Color.Black else sectorColor,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = if (time != null) formatLapTime(time) else "—",
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (highlight) FontWeight.Normal else FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun LapList(laps: List<CompletedLap>, modifier: Modifier = Modifier) {
    if (laps.isEmpty()) {
        Text(
            "Las vueltas completas van a aparecer acá.\n🟣 récord personal · 🟢 mejor de la sesión · 🟡 más lento",
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { LapRowHeader() }
        // La vuelta más reciente arriba.
        items(laps.reversed(), key = { it.number }) { lap ->
            LapRow(lap)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
        }
    }
}

@Composable
private fun LapRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        listOf("V", "S1", "S2", "S3", "Vuelta").forEachIndexed { index, title ->
            Text(
                title,
                modifier = if (index == 0) Modifier.width(32.dp) else Modifier.weight(if (index == 4) 1.4f else 1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                textAlign = if (index == 0) TextAlign.Start else TextAlign.End
            )
        }
    }
}

@Composable
private fun LapRow(lap: CompletedLap) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(lap.number.toString(), modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold)
        lap.sectors.forEach { sector ->
            Text(
                formatLapTime(sector.timeMillis),
                modifier = Modifier.weight(1f),
                color = sector.color.toColor(),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                fontSize = 13.sp
            )
        }
        Text(
            formatLapTime(lap.lapTimeMillis),
            modifier = Modifier.weight(1.4f),
            color = lap.lapColor.toColor(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}
