package com.agussnb.circuitmakerf1.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agussnb.circuitmakerf1.domain.model.Track
import com.agussnb.circuitmakerf1.domain.port.TrackRepository
import com.agussnb.circuitmakerf1.ui.components.AddButton

@Composable
fun DetailsScreen(repository : TrackRepository, trackId : Int,
                  onBack : () -> Unit, onErase : () -> Unit){
    val track by produceState<Track?>(initialValue = null, key1 = trackId) {
        value = repository.getTrackById(trackId)
    }
    Column{
        AddButton(onClick = onBack, label = "Volver", modifier = Modifier.padding(top = 20.dp))
        if (track != null) {
            Text(track.toString(), modifier = Modifier.padding(16.dp))
        } else {
            Text("Cargando información o circuito no encontrado...", modifier = Modifier.padding(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        AddButton(onClick = onErase, label = "Borrar circuito")
    }
}