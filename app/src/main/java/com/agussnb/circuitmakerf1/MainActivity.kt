package com.agussnb.circuitmakerf1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.agussnb.circuitmakerf1.ui.navigation.AppNavigation
import com.agussnb.circuitmakerf1.ui.theme.CircuitMakerF1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CircuitMakerF1Theme {
                AppNavigation()
            }
        }
    }
}




