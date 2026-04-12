package com.io.dronecontroller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.io.dronecontroller.service.MavlinkForegroundService

class MainActivity : ComponentActivity() {
    private var droneService: MavlinkForegroundService? = null
    private var isBound = false

    private val serviceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?,
            ) {
                droneService = (service as MavlinkForegroundService.LocalBinder).getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                droneService = null
                isBound = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(onStartService = {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, MavlinkForegroundService::class.java),
                )
            })
        }
    }

    override fun onStart() {
        super.onStart()
        MavlinkForegroundService.start(this)
        Intent(this, MavlinkForegroundService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            droneService = null
        }
    }

    override fun onDestroy() {
        MavlinkForegroundService.stop(this)
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

