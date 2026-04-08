package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.model.RunStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// iOS / JVM 向けスタブ。将来対応。
class MissionDataSourceStub : MissionDataSourceContract {
    override suspend fun uploadMission(items: List<MissionItem>): RunStatus<Unit> =
        RunStatus.Error("未実装")
    override suspend fun startMission(): RunStatus<Unit> = RunStatus.Error("未実装")
    override suspend fun stopMission(): RunStatus<Unit> = RunStatus.Error("未実装")
    override suspend fun pauseMission(): RunStatus<Unit> = RunStatus.Error("未実装")
    override fun observeMissionProgress(): Flow<MissionProgress> = emptyFlow()
}
