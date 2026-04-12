package com.io.dronecontroller.ui.controller

import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.RunStatus

data class DroneControllerUiState(
    val altitudeMeters: Float = 0f,
    val batteryPercent: Int = 0,
    val speedKmh: Float = 0f,
    val satelliteCount: Int = 0,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val isArmed: Boolean = false,
    val commandStatus: RunStatus<Unit>? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val bearing: Float = 0f,
    val isMapMode: Boolean = false,
    val bleConnectionStatus: BleConnectionStatus = BleConnectionStatus.Disconnected,
    val bleControllerState: BleControllerState = BleControllerState(),
    val errorMessage: String? = null,
    val isReconnecting: Boolean = false,
    val isCapturingPhoto: Boolean = false,
    val isRecording: Boolean = false,
    val isReturningToLaunch: Boolean = false,
) {
    val connectedControllerName: String?
        get() = (bleConnectionStatus as? BleConnectionStatus.Connected)?.device?.name
}
