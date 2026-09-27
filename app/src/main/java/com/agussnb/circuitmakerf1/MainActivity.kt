package com.agussnb.circuitmakerf1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import com.agussnb.circuitmakerf1.data.CircuitRepository
import com.agussnb.circuitmakerf1.database.AppDatabase
import com.agussnb.circuitmakerf1.gps.GPSManager
import com.agussnb.circuitmakerf1.navigation.AppNavigation
import com.agussnb.circuitmakerf1.ui.theme.CircuitMakerF1Theme
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
        setContent {
            CircuitMakerF1Theme {
                AppNavigation(repository=repository, gpsManager=gpsManager)
            }
        }
    }
}




