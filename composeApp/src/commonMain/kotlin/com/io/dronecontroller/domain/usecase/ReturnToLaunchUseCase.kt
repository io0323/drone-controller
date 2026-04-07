package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract

class ReturnToLaunchUseCase(
    private val repository: MavlinkRepositoryContract
) : ReturnToLaunchUseCaseContract {
    override suspend operator fun invoke(): RunStatus<Unit> =
        repository.returnToLaunch()
}
