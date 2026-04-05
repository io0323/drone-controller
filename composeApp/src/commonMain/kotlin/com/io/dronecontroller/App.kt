package com.io.dronecontroller

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.io.dronecontroller.ui.connection.ConnectionScreen

@Composable
fun App() {
    MaterialTheme {
        ConnectionScreen()
    }
}
