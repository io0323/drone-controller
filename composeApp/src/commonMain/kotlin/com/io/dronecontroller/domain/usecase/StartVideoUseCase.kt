package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract

class StartVideoUseCase(
    private val repository: MavlinkRepositoryContract,
) : StartVideoUseCaseContract {
    override suspend operator fun invoke(): RunStatus<Unit> = repository.startVideo()
}
