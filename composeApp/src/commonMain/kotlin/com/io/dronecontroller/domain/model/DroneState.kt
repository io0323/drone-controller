package com.io.dronecontroller.domain.model

data class DroneState(
    val altitudeMeters: Float = 0f,
    val batteryPercent: Int = 0,
    val speedKmh: Float = 0f,
    val satelliteCount: Int = 0,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val isArmed: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val bearing: Float = 0f,
)
