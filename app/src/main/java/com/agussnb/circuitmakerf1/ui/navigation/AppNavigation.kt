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
import com.agussnb.circuitmakerf1.domain.service.TrackRecorder

const val home = "home"
const val details = "details/{trackId}"
const val record = "record"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = home) {
        composable(home) {
            HomeScreen(
                onTrackClick = { id -> navController.navigate("details/$id") },
                onRecordClick = { navController.navigate(record) }
            )
        }
        composable(
            route = details,
            arguments = listOf(navArgument("trackId") { type = NavType.IntType })
        ) {
            DetailsScreen(onBack = { navController.popBackStack() })
        }
        composable(record) {
            RecordingCircuitScreen(onBack = { navController.popBackStack() })
        }
    }
}
