package com.io.dronecontroller.ui.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionStatus
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.usecase.ObserveMissionProgressUseCaseContract
import com.io.dronecontroller.domain.usecase.PauseMissionUseCaseContract
import com.io.dronecontroller.domain.usecase.StartMissionUseCaseContract
import com.io.dronecontroller.domain.usecase.StopMissionUseCaseContract
import com.io.dronecontroller.domain.usecase.UploadMissionUseCaseContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MissionPlanningViewModel(
    private val uploadMissionUseCase: UploadMissionUseCaseContract,
    private val startMissionUseCase: StartMissionUseCaseContract,
    private val stopMissionUseCase: StopMissionUseCaseContract,
    private val pauseMissionUseCase: PauseMissionUseCaseContract,
    private val observeMissionProgress: ObserveMissionProgressUseCaseContract,
) : ViewModel(),
    MissionPlanningViewModelContract {
    private val _uiState = MutableStateFlow(MissionPlanningUiState())
    override val uiState: StateFlow<MissionPlanningUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                observeMissionProgress().collect { progress ->
                    val status = if (progress.isComplete) MissionStatus.Complete else MissionStatus.Running
                    _uiState.update { it.copy(progress = progress, missionStatus = status) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "接続エラー") }
            }
        }
    }

    override fun addWaypoint(
        latitudeDeg: Double,
        longitudeDeg: Double,
    ) {
        val newItem =
            MissionItem(
                latitudeDeg = latitudeDeg,
                longitudeDeg = longitudeDeg,
                altitudeMeters = 10f,
                speedMS = 5f,
            )
        _uiState.update { it.copy(waypoints = it.waypoints + newItem) }
    }

    override fun updateWaypoint(
        index: Int,
        item: MissionItem,
    ) {
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
        val waypoints = _uiState.value.waypoints
        if (waypoints.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "ウェイポイントが設定されていません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(missionStatus = MissionStatus.Uploading, commandStatus = RunStatus.Loading) }
            val result = uploadMissionUseCase(waypoints)
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    missionStatus = if (result is RunStatus.Success) MissionStatus.Idle else MissionStatus.Error,
                    errorMessage = (result as? RunStatus.Error)?.message,
                )
            }
        }
    }

    override fun startMission() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading) }
            val result = startMissionUseCase()
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    missionStatus = if (result is RunStatus.Success) MissionStatus.Running else MissionStatus.Error,
                    errorMessage = (result as? RunStatus.Error)?.message,
                )
            }
        }
    }

    override fun stopMission() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading) }
            val result = stopMissionUseCase()
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    missionStatus = if (result is RunStatus.Success) MissionStatus.Idle else MissionStatus.Error,
                    errorMessage = (result as? RunStatus.Error)?.message,
                )
            }
        }
    }

    override fun pauseMission() {
        viewModelScope.launch {
            _uiState.update { it.copy(commandStatus = RunStatus.Loading) }
            val result = pauseMissionUseCase()
            _uiState.update {
                it.copy(
                    commandStatus = result,
                    missionStatus = if (result is RunStatus.Success) MissionStatus.Paused else MissionStatus.Error,
                    errorMessage = (result as? RunStatus.Error)?.message,
                )
            }
        }
    }

    override fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
