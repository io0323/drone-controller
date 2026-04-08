package com.io.dronecontroller.ui.ble

import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MockBleControllerViewModel : BleControllerViewModelContract {
    private val _uiState = MutableStateFlow(
        BleControllerUiState(
            connectionStatus = BleConnectionStatus.Disconnected,
            scannedDevices = listOf(
                BleDevice(name = "GameSir T4 Pro", address = "AA:BB:CC:DD:EE:01"),
                BleDevice(name = "DualSense Wireless", address = "AA:BB:CC:DD:EE:02"),
                BleDevice(name = "Unknown (FF:03)", address = "AA:BB:CC:DD:FF:03")
            )
        )
    )
    override val uiState: StateFlow<BleControllerUiState> = _uiState

    override fun startScan() {
        _uiState.update { it.copy(isScanning = true) }
    }

    override fun stopScan() {
        _uiState.update { it.copy(isScanning = false) }
    }

    override fun connect(address: String) {
        val device = _uiState.value.scannedDevices.firstOrNull { it.address == address }
            ?: return
        _uiState.update { it.copy(connectionStatus = BleConnectionStatus.Connected(device)) }
    }

    override fun disconnect() {
        _uiState.update { it.copy(connectionStatus = BleConnectionStatus.Disconnected) }
    }
}
