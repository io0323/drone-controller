package com.io.dronecontroller.ui.mission

import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.model.MissionStatus
import com.io.dronecontroller.domain.model.RunStatus

data class MissionPlanningUiState(
    val waypoints: List<MissionItem> = emptyList(),
    val missionStatus: MissionStatus = MissionStatus.Idle,
    val progress: MissionProgress? = null,
    val commandStatus: RunStatus<Unit>? = null,
    val errorMessage: String? = null,
)
