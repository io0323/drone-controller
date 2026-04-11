package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MissionRepositoryContract

class StartMissionUseCase(
    private val repository: MissionRepositoryContract,
) : StartMissionUseCaseContract {
    override suspend operator fun invoke(): RunStatus<Unit> = repository.startMission()
}
