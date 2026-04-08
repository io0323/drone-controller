package com.io.dronecontroller.ui.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.usecase.LandUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveDroneStateUseCaseContract
import com.io.dronecontroller.domain.usecase.ReturnToLaunchUseCaseContract
import com.io.dronecontroller.domain.usecase.TakeoffUseCaseContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DroneControllerViewModel(
    private val observeDroneState: ObserveDroneStateUseCaseContract,
    private val takeoffUseCase: TakeoffUseCaseContract,
    private val landUseCase: LandUseCaseContract,
    private val returnToLaunchUseCase: ReturnToLaunchUseCaseContract
) : ViewModel(), DroneControllerViewModelContract {

    private val _uiState = MutableStateFlow(DroneControllerUiState())
    override val uiState: StateFlow<DroneControllerUiState> = _uiState.asStateFlow()

    private var observingJob: Job? = null

    override fun startObserving(address: String, port: Int) {
        observingJob?.cancel()
        observingJob = viewModelScope.launch {
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
            }
        }
    }

    override fun stopObserving() {
        observingJob?.cancel()
        observingJob = null
    }

    override fun takeoff(altitude: Float) {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading) }
            val result = takeoffUseCase(altitude)
            _uiState.update { it.copy(commandStatus = result) }
        }
    }

    override fun land() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading) }
            val result = landUseCase()
            _uiState.update { it.copy(commandStatus = result) }
        }
    }

    override fun returnToLaunch() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading) }
            val result = returnToLaunchUseCase()
            _uiState.update { it.copy(commandStatus = result) }
        }
    }

    override fun toggleMapMode() {
        _uiState.update { it.copy(isMapMode = !it.isMapMode) }
    }
}
