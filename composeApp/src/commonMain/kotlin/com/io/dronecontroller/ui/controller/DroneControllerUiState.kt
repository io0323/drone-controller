package com.io.dronecontroller.ui.controller

import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.RunStatus

data class DroneControllerUiState(
    val altitudeMeters: Float = 0f,
    val batteryPercent: Int = 0,
    val speedKmh: Float = 0f,
    val satelliteCount: Int = 0,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val isArmed: Boolean = false,
    val commandStatus: RunStatus<Unit>? = null
)
