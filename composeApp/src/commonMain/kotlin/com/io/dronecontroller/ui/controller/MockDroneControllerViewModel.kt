package com.io.dronecontroller.ui.controller

import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.RunStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MockDroneControllerViewModel : DroneControllerViewModelContract {
    private val _uiState = MutableStateFlow(
        DroneControllerUiState(
            altitudeMeters = 25.3f,
            batteryPercent = 87,
            speedKmh = 12.5f,
            satelliteCount = 12,
            connectionStatus = ConnectionStatus.Connected(lastHeartbeatAt = 0L),
            isArmed = true
        )
    )
    override val uiState: StateFlow<DroneControllerUiState> = _uiState

    override fun startObserving(address: String, port: Int) = Unit
    override fun stopObserving() = Unit

    override fun takeoff(altitude: Float) {
        _uiState.update { it.copy(commandStatus = RunStatus.Success(Unit)) }
    }

    override fun land() {
        _uiState.update { it.copy(commandStatus = RunStatus.Success(Unit)) }
    }

    override fun returnToLaunch() {
        _uiState.update { it.copy(commandStatus = RunStatus.Success(Unit)) }
    }
}
