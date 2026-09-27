package com.agussnb.circuitmakerf1

import android.app.Application
import com.agussnb.circuitmakerf1.di.AppContainer

class CircuitMakerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}