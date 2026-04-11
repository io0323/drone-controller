package com.io.dronecontroller.service

data class DroneNotificationState(
    val batteryPercent: Int = 0,
    val isConnected: Boolean = false,
)
