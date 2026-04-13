package com.io.dronecontroller.ui.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.usecase.CapturePhotoUseCaseContract
import com.io.dronecontroller.domain.usecase.LandUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveBleConnectionStatusUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveBleControllerStateUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveDroneStateUseCaseContract
import com.io.dronecontroller.domain.usecase.ReturnToLaunchUseCaseContract
import com.io.dronecontroller.domain.usecase.SendManualControlUseCaseContract
import com.io.dronecontroller.domain.usecase.StartVideoUseCaseContract
import com.io.dronecontroller.domain.usecase.StopVideoUseCaseContract
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
    private val observeBleConnectionStatus: ObserveBleConnectionStatusUseCaseContract,
    private val droneStateHolder: DroneStateHolder,
    private val capturePhotoUseCase: CapturePhotoUseCaseContract,
    private val startVideoUseCase: StartVideoUseCaseContract,
    private val stopVideoUseCase: StopVideoUseCaseContract,
) : ViewModel(),
    DroneControllerViewModelContract {
    private val _uiState = MutableStateFlow(DroneControllerUiState())
    override val uiState: StateFlow<DroneControllerUiState> = _uiState.asStateFlow()

    private val _virtualJoystick = MutableStateFlow(BleControllerState())
    private var observingJob: Job? = null

    private var currentAddress: String = "10.0.2.2"
    private var currentPort: Int = 50051
    private var reconnectCount = 0
    private var previousConnectionStatus: ConnectionStatus = ConnectionStatus.Disconnected

    override fun startObserving(
        address: String,
        port: Int,
    ) {
        currentAddress = address
        currentPort = port
        reconnectCount = 0
        startObservingInternal()
    }

    private fun startObservingInternal() {
        observingJob?.cancel()
        previousConnectionStatus = ConnectionStatus.Disconnected
        observingJob =
            viewModelScope.launch {
                launch {
                    observeDroneState(currentAddress, currentPort).collect { state ->
                        val wasConnected = previousConnectionStatus is ConnectionStatus.Connected
                        val isDisconnected =
                            state.connectionStatus is ConnectionStatus.Disconnected ||
                                state.connectionStatus is ConnectionStatus.Error

                        if (wasConnected && isDisconnected) {
                            handleUnexpectedDisconnect()
                            return@collect
                        }

                        if (state.connectionStatus is ConnectionStatus.Connected && reconnectCount > 0) {
                            reconnectCount = 0
                            _uiState.update { it.copy(isReconnecting = false) }
                        }

                        previousConnectionStatus = state.connectionStatus
                        _uiState.update { current ->
                            current.copy(
                                altitudeMeters = state.altitudeMeters,
                                batteryPercent = state.batteryPercent,
                                speedKmh = state.speedKmh,
                                satelliteCount = state.satelliteCount,
                                connectionStatus = state.connectionStatus,
                                isArmed = state.isArmed,
                                latitude = state.latitude,
                                longitude = state.longitude,
                                bearing = state.bearing,
                                isReturningToLaunch = current.isReturningToLaunch && state.isArmed,
                            )
                        }
                        droneStateHolder.update(
                            batteryPercent = state.batteryPercent,
                            isConnected = state.connectionStatus is ConnectionStatus.Connected,
                        )
                    }
                }
                launch {
                    observeBleControllerState().collect { bleState ->
                        _uiState.update { it.copy(bleControllerState = bleState) }
                    }
                }
                launch {
                    observeBleConnectionStatus().collect { status ->
                        _uiState.update { it.copy(bleConnectionStatus = status) }
                    }
                }
                launch {
                    while (true) {
                        val state = _uiState.value
                        if (state.connectionStatus is ConnectionStatus.Connected) {
                            val input =
                                if (state.bleConnectionStatus is BleConnectionStatus.Connected) {
                                    state.bleControllerState
                                } else {
                                    _virtualJoystick.value
                                }
                            sendManualControl(
                                pitch = -input.rightY,
                                roll = input.rightX,
                                throttle = input.leftY,
                                yaw = input.leftX,
                            )
                        }
                        delay(100L)
                    }
                }
            }
    }

    private fun handleUnexpectedDisconnect() {
        if (reconnectCount >= 3) {
            _uiState.update {
                it.copy(isReconnecting = false, isReturningToLaunch = false, errorMessage = "接続が切断されました。再接続に失敗しました")
            }
            return
        }
        reconnectCount++
        _uiState.update {
            it.copy(isReconnecting = true, isReturningToLaunch = false, errorMessage = "接続断。再接続中… ($reconnectCount/3)")
        }
        viewModelScope.launch {
            observingJob?.cancel()
            delay(5000L)
            startObservingInternal()
        }
    }

    private suspend fun executeWithRetry(
        errorMessage: String,
        action: suspend () -> RunStatus<Unit>,
    ): RunStatus<Unit> {
        var lastResult: RunStatus<Unit> = RunStatus.Error(errorMessage)
        repeat(3) { attempt ->
            lastResult = action()
            if (lastResult is RunStatus.Success) return lastResult
            if (attempt < 2) delay(1000L)
        }
        return RunStatus.Error(errorMessage)
    }

    override fun stopObserving() {
        observingJob?.cancel()
        observingJob = null
    }

    override fun updateJoystickInput(
        leftX: Float,
        leftY: Float,
        rightX: Float,
        rightY: Float,
    ) {
        _virtualJoystick.update {
            it.copy(leftX = leftX, leftY = leftY, rightX = rightX, rightY = rightY)
        }
    }

    override fun takeoff(altitude: Float) {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading, errorMessage = null) }
            val result = executeWithRetry("離陸コマンドが失敗しました") { takeoffUseCase(altitude) }
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    errorMessage = (result as? RunStatus.Error)?.message,
                )
            }
        }
    }

    override fun land() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading, errorMessage = null) }
            val result = executeWithRetry("着陸コマンドが失敗しました") { landUseCase() }
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    errorMessage = (result as? RunStatus.Error)?.message,
                )
            }
        }
    }

    override fun returnToLaunch() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading, errorMessage = null) }
            val result = executeWithRetry("帰還コマンドが失敗しました") { returnToLaunchUseCase() }
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    errorMessage = (result as? RunStatus.Error)?.message,
                    isReturningToLaunch = result is RunStatus.Success,
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

    override fun capturePhoto() {
        if (_uiState.value.isCapturingPhoto) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturingPhoto = true, errorMessage = null) }
            val result = capturePhotoUseCase()
            _uiState.update {
                it.copy(
                    isCapturingPhoto = false,
                    errorMessage = (result as? RunStatus.Error)?.message,
                )
            }
        }
    }

    override fun toggleRecording() {
        val isRecording = _uiState.value.isRecording
        viewModelScope.launch {
            if (isRecording) {
                val result = stopVideoUseCase()
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        errorMessage = (result as? RunStatus.Error)?.message,
                    )
                }
            } else {
                val result = startVideoUseCase()
                if (result is RunStatus.Success) {
                    _uiState.update { it.copy(isRecording = true) }
                } else {
                    _uiState.update {
                        it.copy(errorMessage = (result as? RunStatus.Error)?.message)
                    }
                }
            }
        }
    }
}
