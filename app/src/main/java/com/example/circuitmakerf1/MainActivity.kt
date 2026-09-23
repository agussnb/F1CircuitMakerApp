package com.example.circuitmakerf1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.room.Room
import com.example.circuitmakerf1.data.CircuitRepository
import com.example.circuitmakerf1.database.AppDatabase
import com.example.circuitmakerf1.gps.GPSManager
import com.example.circuitmakerf1.model.Circuit
import com.example.circuitmakerf1.navigation.AppNavigation
import com.example.circuitmakerf1.ui.theme.CircuitMakerF1Theme
import com.example.circuitmakerf1.ui.HomeScreen
import kotlinx.coroutines.launch
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val gpsManager = GPSManager(this)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "circuit_database"
        ).build()
        val dao = db.circuitDao()
        val repository = CircuitRepository(dao)
        lifecycleScope.launch {
            repository.insertsCircuit(
                Circuit(name = "Monza", country = "Italy", lengthKm = 55.412, laps = 53)
            )
        }
        setContent {
            CircuitMakerF1Theme {
                AppNavigation(repository=repository, gpsManager=gpsManager)
            }
        }
    }
}




