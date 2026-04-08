package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MissionRepositoryContract

class UploadMissionUseCase(
    private val repository: MissionRepositoryContract
) : UploadMissionUseCaseContract {
    override suspend operator fun invoke(items: List<MissionItem>): RunStatus<Unit> =
        repository.uploadMission(items)
}
