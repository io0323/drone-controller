package com.io.dronecontroller

import android.app.Application
import com.io.dronecontroller.di.initKoin
import org.koin.android.ext.koin.androidContext

class DroneControllerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@DroneControllerApp)
        }
    }
}
