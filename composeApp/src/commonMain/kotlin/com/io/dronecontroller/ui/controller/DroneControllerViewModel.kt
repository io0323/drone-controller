package com.io.dronecontroller.ui.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.usecase.LandUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveBleConnectionStatusUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveBleControllerStateUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveDroneStateUseCaseContract
import com.io.dronecontroller.domain.usecase.ReturnToLaunchUseCaseContract
import com.io.dronecontroller.domain.usecase.SendManualControlUseCaseContract
import com.io.dronecontroller.domain.usecase.TakeoffUseCaseContract
import com.io.dronecontroller.service.DroneStateHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DroneControllerViewModel(
    private val observeDroneState: ObserveDroneStateUseCaseContract,
    private val takeoffUseCase: TakeoffUseCaseContract,
    private val landUseCase: LandUseCaseContract,
    private val returnToLaunchUseCase: ReturnToLaunchUseCaseContract,
    private val sendManualControl: SendManualControlUseCaseContract,
    private val observeBleControllerState: ObserveBleControllerStateUseCaseContract,
    private val droneStateHolder: DroneStateHolder
) : ViewModel(), DroneControllerViewModelContract {

    private val _uiState = MutableStateFlow(DroneControllerUiState())
    override val uiState: StateFlow<DroneControllerUiState> = _uiState.asStateFlow()

    private val _virtualJoystick = MutableStateFlow(BleControllerState())
    private var observingJob: Job? = null

    override fun startObserving(address: String, port: Int) {
        observingJob?.cancel()
        observingJob = viewModelScope.launch {
            // ドローン状態の観測
            launch {
                observeDroneState(address, port).collect { state ->
                    _uiState.update {
                        it.copy(
                            altitudeMeters = state.altitudeMeters,
                            batteryPercent = state.batteryPercent,
                            speedKmh = state.speedKmh,
                            satelliteCount = state.satelliteCount,
                            connectionStatus = state.connectionStatus,
                            isArmed = state.isArmed,
                            latitude = state.latitude,
                            longitude = state.longitude,
                            bearing = state.bearing
                        )
                    }
                    // Foreground Service の通知状態を更新
                    droneStateHolder.update(
                        batteryPercent = state.batteryPercent,
                        isConnected = state.connectionStatus is ConnectionStatus.Connected
                    )
                }
            }
            // BLEコントローラー状態の観測
            launch {
                observeBleControllerState().collect { bleState ->
                    _uiState.update { it.copy(bleControllerState = bleState) }
                }
            }
            // 手動制御ループ（10Hz）: BLE接続時は物理コントローラー優先
            launch {
                while (true) {
                    val state = _uiState.value
                    if (state.connectionStatus is ConnectionStatus.Connected) {
                        val input = if (state.bleConnectionStatus is BleConnectionStatus.Connected) {
                            state.bleControllerState
                        } else {
                            _virtualJoystick.value
                        }
                        sendManualControl(
                            pitch = -input.rightY,
                            roll = input.rightX,
                            throttle = input.leftY,
                            yaw = input.leftX
                        )
                    }
                    delay(100L)
                }
            }
        }
    }

    override fun stopObserving() {
        observingJob?.cancel()
        observingJob = null
    }

    override fun updateJoystickInput(leftX: Float, leftY: Float, rightX: Float, rightY: Float) {
        _virtualJoystick.update {
            it.copy(leftX = leftX, leftY = leftY, rightX = rightX, rightY = rightY)
        }
    }

    override fun takeoff(altitude: Float) {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading, errorMessage = null) }
            val result = takeoffUseCase(altitude)
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    errorMessage = (result as? RunStatus.Error)?.message
                )
            }
        }
    }

    override fun land() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading, errorMessage = null) }
            val result = landUseCase()
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    errorMessage = (result as? RunStatus.Error)?.message
                )
            }
        }
    }

    override fun returnToLaunch() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading, errorMessage = null) }
            val result = returnToLaunchUseCase()
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    errorMessage = (result as? RunStatus.Error)?.message
                )
            }
        }
    }

    override fun toggleMapMode() {
        _uiState.update { it.copy(isMapMode = !it.isMapMode) }
    }

    override fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
