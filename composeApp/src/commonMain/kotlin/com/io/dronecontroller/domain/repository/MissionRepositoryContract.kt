package com.io.dronecontroller.domain.repository

import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.model.RunStatus
import kotlinx.coroutines.flow.Flow

interface MissionRepositoryContract {
    suspend fun uploadMission(items: List<MissionItem>): RunStatus<Unit>
    suspend fun startMission(): RunStatus<Unit>
    suspend fun stopMission(): RunStatus<Unit>
    suspend fun pauseMission(): RunStatus<Unit>
    fun observeMissionProgress(): Flow<MissionProgress>
}
