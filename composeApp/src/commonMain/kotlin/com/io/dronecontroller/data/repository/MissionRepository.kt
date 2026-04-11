package com.io.dronecontroller.data.repository

import com.io.dronecontroller.data.datasource.MissionDataSourceContract
import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MissionRepositoryContract
import kotlinx.coroutines.flow.Flow

class MissionRepository(
    private val dataSource: MissionDataSourceContract,
) : MissionRepositoryContract {
    override suspend fun uploadMission(items: List<MissionItem>): RunStatus<Unit> = dataSource.uploadMission(items)

    override suspend fun startMission(): RunStatus<Unit> = dataSource.startMission()

    override suspend fun stopMission(): RunStatus<Unit> = dataSource.stopMission()

    override suspend fun pauseMission(): RunStatus<Unit> = dataSource.pauseMission()

    override fun observeMissionProgress(): Flow<MissionProgress> = dataSource.observeMissionProgress()
}
