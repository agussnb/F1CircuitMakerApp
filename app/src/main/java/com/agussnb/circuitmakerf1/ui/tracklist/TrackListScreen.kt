package com.agussnb.circuitmakerf1.ui.tracklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.ui.components.ScreenScaffold
import com.agussnb.circuitmakerf1.ui.components.emoji
import com.agussnb.circuitmakerf1.ui.components.formatKm
import com.agussnb.circuitmakerf1.ui.components.label
import com.agussnb.circuitmakerf1.ui.theme.F1Red

@Composable
fun TrackListScreen(
    onBack: () -> Unit,
    onTrackClick: (Int) -> Unit,
    onCreateClick: () -> Unit,
    viewModel: TrackListViewModel = viewModel(factory = TrackListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenScaffold(
        title = "Mis circuitos",
        onBack = onBack,
        floatingActionButton = {
            if (uiState.tracks.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onCreateClick,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Crear circuito") }
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.tracks.isEmpty() -> EmptyState(
                onCreateClick = onCreateClick,
                modifier = Modifier.padding(padding)
            )

            // LazyColumn: solo compone las tarjetas visibles en pantalla (como un RecyclerView).
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.tracks, key = { it.id }) { track ->
                    TrackCard(track = track, onClick = { onTrackClick(track.id) })
                }
            }
        }
    }
}

@Composable
private fun TrackCard(track: Track, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = listOf(track.country, "${track.sportType.emoji()} ${track.sportType.label()}")
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatKm(track.lengthKm, decimals = 2), fontWeight = FontWeight.SemiBold)
                Text(
                    "${track.laps} vueltas",
                    style = MaterialTheme.typography.bodySmall,
                    color = F1Red
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onCreateClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏁", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            "Todavía no creaste circuitos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Grabá un recorrido con el GPS y volvé al punto de partida para cerrarlo.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreateClick) { Text("Crear mi primer circuito") }
    }
}
