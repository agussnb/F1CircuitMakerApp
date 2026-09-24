package com.example.circuitmakerf1.ui

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.circuitmakerf1.data.CircuitRepository
import com.example.circuitmakerf1.model.Circuit
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(repository : CircuitRepository, onCircuitClick : (String)-> Unit, onRecordClick : () -> Unit){
    //val listaCircuitos : mutableStateListOf<Circuit> = CircuitRepository.getCircuits();
    val listaCircuitos by repository.allCircuits.collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()){ //Columna mostrando circuitos

        Spacer(modifier = Modifier.height(8.dp))

        AddButton(onClick = onRecordClick, //Boton para ir a la pantalla de grabacion
            modifier = Modifier.fillMaxWidth(),
            label = "Pantalla de grabacion")

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.verticalScroll(scrollState)){
            listaCircuitos.forEach { circuit ->
                CircuitItem(circuit = circuit,
                    modifier = Modifier.clickable { onCircuitClick(circuit.name) }.padding(top = 10.dp))

            }
        }
    }
}

@Composable
fun CircuitItem(circuit: Circuit, modifier: Modifier = Modifier){
    Text(text = "$circuit",
        modifier = modifier.padding(top=15.dp))
}