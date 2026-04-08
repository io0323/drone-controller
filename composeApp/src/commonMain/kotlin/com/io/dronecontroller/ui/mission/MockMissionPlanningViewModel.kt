package com.io.dronecontroller.ui.mission

import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.model.MissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MockMissionPlanningViewModel : MissionPlanningViewModelContract {
    private val _uiState = MutableStateFlow(
        MissionPlanningUiState(
            waypoints = listOf(
                MissionItem(35.6762, 139.6503, 10f, 5f),
                MissionItem(35.6772, 139.6513, 15f, 5f),
                MissionItem(35.6752, 139.6523, 20f, 3f)
            ),
            missionStatus = MissionStatus.Idle,
            progress = MissionProgress(currentItemIndex = 1, missionCount = 3)
        )
    )
    override val uiState: StateFlow<MissionPlanningUiState> = _uiState

    override fun addWaypoint(latitudeDeg: Double, longitudeDeg: Double) {
        _uiState.update {
            it.copy(waypoints = it.waypoints + MissionItem(latitudeDeg, longitudeDeg, 10f, 5f))
        }
    }

    override fun updateWaypoint(index: Int, item: MissionItem) {
        val updated = _uiState.value.waypoints.toMutableList()
        if (index in updated.indices) {
            updated[index] = item
            _uiState.update { it.copy(waypoints = updated) }
        }
    }

    override fun removeWaypoint(index: Int) {
        val updated = _uiState.value.waypoints.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _uiState.update { it.copy(waypoints = updated) }
        }
    }

    override fun uploadMission() {
        _uiState.update { it.copy(missionStatus = MissionStatus.Idle) }
    }

    override fun startMission() {
        _uiState.update { it.copy(missionStatus = MissionStatus.Running) }
    }

    override fun stopMission() {
        _uiState.update { it.copy(missionStatus = MissionStatus.Idle) }
    }

    override fun pauseMission() {
        _uiState.update { it.copy(missionStatus = MissionStatus.Paused) }
    }

    override fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
