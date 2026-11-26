package com.agussnb.circuitmakerf1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agussnb.circuitmakerf1.ui.detail.DetailsScreen
import com.agussnb.circuitmakerf1.ui.menu.MenuScreen
import com.agussnb.circuitmakerf1.ui.options.OptionsScreen
import com.agussnb.circuitmakerf1.ui.recording.RecordingCircuitScreen
import com.agussnb.circuitmakerf1.ui.session.LapSessionScreen
import com.agussnb.circuitmakerf1.ui.tracklist.TrackListScreen

const val menu = "menu"
const val tracks = "tracks"
const val details = "details/{trackId}"
const val record = "record"
const val options = "options"
const val session = "session/{trackId}"

private val trackIdArgument = listOf(navArgument("trackId") { type = NavType.IntType })

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = menu) {
        composable(menu) {
            MenuScreen(
                onMyTracksClick = { navController.navigate(tracks) },
                onCreateTrackClick = { navController.navigate(record) },
                onOptionsClick = { navController.navigate(options) }
            )
        }
        composable(tracks) {
            TrackListScreen(
                onBack = back,
                onTrackClick = { id -> navController.navigate("details/$id") },
                onCreateClick = { navController.navigate(record) }
            )
        }
        composable(route = details, arguments = trackIdArgument) {
            DetailsScreen(
                onBack = back,
                onStartSession = { id -> navController.navigate("session/$id") }
            )
        }
        composable(record) {
            RecordingCircuitScreen(
                onBack = back,
                onTrackSaved = { id ->
                    // Al guardar, vamos al detalle del circuito nuevo y sacamos "grabar" del historial,
                    // así el botón "volver" no regresa a una grabación ya terminada.
                    navController.navigate("details/$id") {
                        popUpTo(record) { inclusive = true }
                    }
                }
            )
        }
        composable(route = session, arguments = trackIdArgument) {
            LapSessionScreen(onBack = back)
        }
        composable(options) {
            OptionsScreen(onBack = back)
        }
    }
}
