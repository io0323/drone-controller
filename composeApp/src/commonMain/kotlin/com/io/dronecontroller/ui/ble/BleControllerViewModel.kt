package com.io.dronecontroller.ui.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.usecase.ConnectBleDeviceUseCaseContract
import com.io.dronecontroller.domain.usecase.DisconnectBleDeviceUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveBleConnectionStatusUseCaseContract
import com.io.dronecontroller.domain.usecase.ScanBleDevicesUseCaseContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BleControllerViewModel(
    private val scanBleDevices: ScanBleDevicesUseCaseContract,
    private val connectBleDevice: ConnectBleDeviceUseCaseContract,
    private val disconnectBleDevice: DisconnectBleDeviceUseCaseContract,
    private val observeBleConnectionStatus: ObserveBleConnectionStatusUseCaseContract,
) : ViewModel(),
    BleControllerViewModelContract {
    private val _uiState = MutableStateFlow(BleControllerUiState())
    override val uiState: StateFlow<BleControllerUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        viewModelScope.launch {
            observeBleConnectionStatus().collect { status ->
                _uiState.update {
                    it.copy(
                        connectionStatus = status,
                        isScanning = status is BleConnectionStatus.Scanning,
                    )
                }
            }
        }
    }

    override fun startScan() {
        scanJob?.cancel()
        scanJob =
            viewModelScope.launch {
                _uiState.update { it.copy(scannedDevices = emptyList(), errorMessage = null) }
                scanBleDevices().collect { devices ->
                    _uiState.update { it.copy(scannedDevices = devices) }
                }
            }
    }

    override fun stopScan() {
        scanJob?.cancel()
        scanJob = null
    }

    override fun connect(address: String) {
        viewModelScope.launch {
            val result = connectBleDevice(address)
            if (result is com.io.dronecontroller.domain.model.RunStatus.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    override fun disconnect() {
        disconnectBleDevice()
    }
}
