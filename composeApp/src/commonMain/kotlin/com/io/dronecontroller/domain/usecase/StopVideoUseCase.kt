package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract

class StopVideoUseCase(
    private val repository: MavlinkRepositoryContract,
) : StopVideoUseCaseContract {
    override suspend operator fun invoke(): RunStatus<Unit> = repository.stopVideo()
}
