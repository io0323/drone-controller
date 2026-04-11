package com.io.dronecontroller

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.io.dronecontroller.service.MavlinkForegroundService

class MainActivity : ComponentActivity() {
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

    override fun onStop() {
        super.onStop()
        // 画面OFF時も接続を維持するためサービスは停止しない
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

