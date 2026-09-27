package com.agussnb.circuitmakerf1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.agussnb.circuitmakerf1.data.location.GPSManager
import com.agussnb.circuitmakerf1.ui.home.HomeScreen
import com.agussnb.circuitmakerf1.ui.detail.DetailsScreen
import com.agussnb.circuitmakerf1.ui.recording.RecordingCircuitScreen
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.agussnb.circuitmakerf1.domain.port.TrackRepository

const val home = "home"
const val details = "details/{trackId}"

const val record = "record"

@Composable
fun AppNavigation(repository: TrackRepository, gpsManager: GPSManager){
    val controller = rememberNavController()
    val scope = rememberCoroutineScope()
    NavHost(navController = controller, startDestination = home){
        composable(home){
            HomeScreen(repository = repository,
                onTrackClick = {name -> controller.navigate("details/$name")},
                onRecordClick = {controller.navigate(record)})
        }
        composable(
            route = details,
            arguments = listOf(navArgument("trackId") { type = NavType.IntType })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getInt("trackId") ?: return@composable
            DetailsScreen(
                repository = repository,
                trackId = trackId,
                onBack = { controller.popBackStack() },
                onErase = {
                    scope.launch {
                        val track = repository.getTrackById(trackId)
                        if (track != null) {
                            repository.deleteTrack(track)
                            controller.popBackStack()
                        }
                    }
                }
            )
        }
        composable(record){
            RecordingCircuitScreen(gpsManager = gpsManager,
                repository = repository,
                onBack={controller.popBackStack()})
        }

    }
}
