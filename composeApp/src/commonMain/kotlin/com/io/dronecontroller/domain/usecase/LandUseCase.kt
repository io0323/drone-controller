package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract

class LandUseCase(
    private val repository: MavlinkRepositoryContract
) : LandUseCaseContract {
    override suspend operator fun invoke(): RunStatus<Unit> =
        repository.land()
}
