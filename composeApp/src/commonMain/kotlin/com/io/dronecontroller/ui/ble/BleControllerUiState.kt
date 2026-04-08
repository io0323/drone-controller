package com.io.dronecontroller.ui.ble

import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleDevice

data class BleControllerUiState(
    val connectionStatus: BleConnectionStatus = BleConnectionStatus.Disconnected,
    val scannedDevices: List<BleDevice> = emptyList(),
    val isScanning: Boolean = false,
    val errorMessage: String? = null
)
