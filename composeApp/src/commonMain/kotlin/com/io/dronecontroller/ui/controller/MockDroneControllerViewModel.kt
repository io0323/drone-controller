package com.io.dronecontroller.ui.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MockDroneControllerViewModel : DroneControllerViewModelContract {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _uiState = MutableStateFlow(DroneControllerUiState())
    override val uiState: StateFlow<DroneControllerUiState> = _uiState.asStateFlow()

    override fun startObserving(address: String, port: Int) {}

    override fun stopObserving() {}

    override fun takeoff(altitude: Float) {}

    override fun land() {}

    override fun returnToLaunch() {}

    override fun toggleMapMode() {
        _uiState.update { it.copy(isMapMode = !it.isMapMode) }
    }

    override fun updateJoystickInput(leftX: Float, leftY: Float, rightX: Float, rightY: Float) {}

    override fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun capturePhoto() {
        if (_uiState.value.isCapturingPhoto) return
        scope.launch {
            _uiState.update { it.copy(isCapturingPhoto = true) }
            delay(400)
            _uiState.update { it.copy(isCapturingPhoto = false) }
        }
    }

    override fun toggleRecording() {
        _uiState.update { it.copy(isRecording = !it.isRecording) }
    }
}
