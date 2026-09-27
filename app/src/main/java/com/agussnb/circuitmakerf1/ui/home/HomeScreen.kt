package com.agussnb.circuitmakerf1.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agussnb.circuitmakerf1.domain.model.Track
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.ui.components.AddButton
import com.agussnb.circuitmakerf1.ui.components.toDisplayText

@Composable
fun HomeScreen(
    onTrackClick: (Int) -> Unit,
    onRecordClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        AddButton(
            onClick = onRecordClick,
            modifier = Modifier.fillMaxWidth(),
            label = "Pantalla de grabación"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            uiState.tracks.forEach { track ->
                TrackItem(
                    track = track,
                    modifier = Modifier
                        .clickable { onTrackClick(track.id) }
                        .padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
fun TrackItem(track: Track, modifier: Modifier = Modifier) {
    Text(
        text = track.toDisplayText(),
        modifier = modifier.padding(top = 15.dp)
    )
}