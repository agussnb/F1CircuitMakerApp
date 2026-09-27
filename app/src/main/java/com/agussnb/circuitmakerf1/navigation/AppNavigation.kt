package com.agussnb.circuitmakerf1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.agussnb.circuitmakerf1.data.CircuitRepository
import com.agussnb.circuitmakerf1.gps.GPSManager
import com.agussnb.circuitmakerf1.ui.HomeScreen
import com.agussnb.circuitmakerf1.ui.DetailsScreen
import com.agussnb.circuitmakerf1.ui.RecordingCircuitScreen
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument

val home = "home"
const val details = "details/{circuitId}"

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
        composable(
            route = details,
            arguments = listOf(navArgument("circuitId") { type = NavType.IntType })
        ) { backStackEntry ->
            val circuitId = backStackEntry.arguments?.getInt("circuitId") ?: return@composable
            DetailsScreen(
                repository = repository,
                circuitId = circuitId,
                onBack = { miController.popBackStack() },
                onErase = {
                    scope.launch {
                        val circuito = repository.getCircuitById(circuitId)
                        if (circuito != null) {
                            repository.deleteCircuit(circuito)
                            miController.popBackStack()
                        }
                    }
                }
            )
        }
        composable(record){
            RecordingCircuitScreen(gpsManager = gpsManager,
                repository = repository,
                onBack={miController.popBackStack()})
        }

    }
}
