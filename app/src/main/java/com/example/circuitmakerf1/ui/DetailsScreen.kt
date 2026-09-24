package com.example.circuitmakerf1.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.circuitmakerf1.data.CircuitRepository
import com.example.circuitmakerf1.model.Circuit

@Composable
fun DetailsScreen(repository : CircuitRepository,circuitId : Int,
                  onBack : () -> Unit, onErase : () -> Unit){
    val circuito by produceState<Circuit?>(initialValue = null, key1 = circuitId) {
        value = repository.getCircuitById(circuitId)
    }
    Column{
        AddButton(onClick = onBack, label = "Volver", modifier = Modifier.padding(top = 20.dp))
        if (circuito != null) {
            Text(circuito.toString(), modifier = Modifier.padding(16.dp))
        } else {
            Text("Cargando información o circuito no encontrado...", modifier = Modifier.padding(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        AddButton(onClick = onErase,label="Borrar circuito")
    }
}