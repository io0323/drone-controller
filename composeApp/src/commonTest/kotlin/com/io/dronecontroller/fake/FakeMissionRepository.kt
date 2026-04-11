package com.io.dronecontroller.fake

import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MissionRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeMissionRepository : MissionRepositoryContract {
    var uploadResult: RunStatus<Unit> = RunStatus.Success(Unit)
    var startResult: RunStatus<Unit> = RunStatus.Success(Unit)
    var stopResult: RunStatus<Unit> = RunStatus.Success(Unit)
    var pauseResult: RunStatus<Unit> = RunStatus.Success(Unit)

    val progressFlow = MutableSharedFlow<MissionProgress>()

    var lastUploadedItems: List<MissionItem> = emptyList()

    override suspend fun uploadMission(items: List<MissionItem>): RunStatus<Unit> {
        lastUploadedItems = items
        return uploadResult
    }

    override suspend fun startMission(): RunStatus<Unit> = startResult

    override suspend fun stopMission(): RunStatus<Unit> = stopResult

    override suspend fun pauseMission(): RunStatus<Unit> = pauseResult

    override fun observeMissionProgress(): Flow<MissionProgress> = progressFlow
}
