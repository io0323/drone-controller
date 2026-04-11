package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MissionRepositoryContract

class StopMissionUseCase(
    private val repository: MissionRepositoryContract,
) : StopMissionUseCaseContract {
    override suspend operator fun invoke(): RunStatus<Unit> = repository.stopMission()
}
