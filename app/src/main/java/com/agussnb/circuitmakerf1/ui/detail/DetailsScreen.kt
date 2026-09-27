package com.agussnb.circuitmakerf1.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.ui.components.AddButton
import com.agussnb.circuitmakerf1.ui.components.toDisplayText

@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        AddButton(onClick = onBack, label = "Volver", modifier = Modifier.padding(top = 20.dp))

        when (val state = uiState) {
            DetailUiState.Loading -> {
                Text("Cargando información...", modifier = Modifier.padding(16.dp))
            }
            DetailUiState.NotFound -> {
                Text("Circuito no encontrado", modifier = Modifier.padding(16.dp))
            }
            is DetailUiState.Success -> {
                Text(state.track.toDisplayText(), modifier = Modifier.padding(16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                AddButton(
                    onClick = { viewModel.deleteTrack(onDeleted = onBack) },
                    label = "Borrar circuito"
                )
            }
        }
    }
}