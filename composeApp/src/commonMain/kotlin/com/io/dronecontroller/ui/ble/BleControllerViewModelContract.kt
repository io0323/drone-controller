package com.io.dronecontroller.ui.ble

import kotlinx.coroutines.flow.StateFlow

interface BleControllerViewModelContract {
    val uiState: StateFlow<BleControllerUiState>
    fun startScan()
    fun stopScan()
    fun connect(address: String)
    fun disconnect()
}
