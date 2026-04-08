package com.io.dronecontroller.domain.model

sealed class BleConnectionStatus {
    object Disconnected : BleConnectionStatus()
    object Scanning : BleConnectionStatus()
    data class Connecting(val device: BleDevice) : BleConnectionStatus()
    data class Connected(val device: BleDevice) : BleConnectionStatus()
    data class Error(val message: String) : BleConnectionStatus()
}
