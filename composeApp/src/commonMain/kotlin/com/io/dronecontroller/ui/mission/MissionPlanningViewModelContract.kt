package com.io.dronecontroller.ui.mission

import com.io.dronecontroller.domain.model.MissionItem
import kotlinx.coroutines.flow.StateFlow

interface MissionPlanningViewModelContract {
    val uiState: StateFlow<MissionPlanningUiState>

    fun addWaypoint(
        latitudeDeg: Double,
        longitudeDeg: Double,
    )

    fun updateWaypoint(
        index: Int,
        item: MissionItem,
    )

    fun removeWaypoint(index: Int)

    fun uploadMission()

    fun startMission()

    fun stopMission()

    fun pauseMission()

    fun clearError()
}
