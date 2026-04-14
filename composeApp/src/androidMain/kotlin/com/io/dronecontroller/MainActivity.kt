package com.io.dronecontroller

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.io.dronecontroller.service.MavlinkForegroundService

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                onStartService = {
                    startDroneService()
                },
                onStopService = {
                    stopDroneService()
                },
                onMock = {
                    mock()
                }
            )
        }
    }

    private fun startDroneService() {
        val intent = Intent(this, MavlinkForegroundService::class.java).apply {
            action = "START"
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopDroneService() {
        val intent = Intent(this, MavlinkForegroundService::class.java)
        stopService(intent)
    }

    private fun mock() {
        val intent = Intent(this, MavlinkForegroundService::class.java).apply {
            action = "START"
        }
        startService(intent)
    }
}
