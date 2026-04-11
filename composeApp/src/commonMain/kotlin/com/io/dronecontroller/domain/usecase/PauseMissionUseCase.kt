package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MissionRepositoryContract

class PauseMissionUseCase(
    private val repository: MissionRepositoryContract,
) : PauseMissionUseCaseContract {
    override suspend operator fun invoke(): RunStatus<Unit> = repository.pauseMission()
}
