package com.example.circuitmakerf1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.circuitmakerf1.data.CircuitRepository
import com.example.circuitmakerf1.gps.GPSManager
import com.example.circuitmakerf1.model.Circuit
import com.example.circuitmakerf1.ui.HomeScreen
import com.example.circuitmakerf1.ui.DetailsScreen
import com.example.circuitmakerf1.ui.RecordingCircuitScreen
import kotlinx.coroutines.launch

val home = "home"
val details = "details/{circuitName}"

const val record = "record"

@Composable
fun AppNavigation(repository: CircuitRepository, gpsManager: GPSManager){
    val miController = rememberNavController()
    val scope = rememberCoroutineScope()
    NavHost(navController = miController, startDestination = home){
        composable(home){
            HomeScreen(repository = repository,
                onCircuitClick = {name -> miController.navigate("details/$name")},
                onRecordClick = {miController.navigate(record)})
        }
        composable(details){backStackEntry ->
            val circuitName = backStackEntry.arguments?.getString("circuitName")
            DetailsScreen(
                repository=repository,
                circuit = circuitName,
                onBack = {miController.popBackStack()},
                onErase = {scope.launch {
                    val circuitoABorrar = repository.getCircuitByName(circuitName!!)
                    if (circuitoABorrar != null) {
                        repository.deleteCircuit(circuitoABorrar)
                        miController.popBackStack()
                    }
                }})
        }
        composable(record){
            RecordingCircuitScreen(gpsManager = gpsManager,
                repository = repository,
                onBack={miController.popBackStack()})
        }

    }
}
