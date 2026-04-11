package com.io.dronecontroller.ui.controller

import kotlinx.coroutines.flow.StateFlow

interface DroneControllerViewModelContract {
    val uiState: StateFlow<DroneControllerUiState>

    fun startObserving(
        address: String = "10.0.2.2",
        port: Int = 50051,
    )

    fun stopObserving()

    fun takeoff(altitude: Float = 5f)

    fun land()

    fun returnToLaunch()

    fun toggleMapMode()

    fun updateJoystickInput(
        leftX: Float,
        leftY: Float,
        rightX: Float,
        rightY: Float,
    )

    fun clearError()
}
