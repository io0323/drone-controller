package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract

class TakeoffUseCase(
    private val repository: MavlinkRepositoryContract,
) : TakeoffUseCaseContract {
    override suspend operator fun invoke(altitudeMeters: Float): RunStatus<Unit> = repository.takeoff(altitudeMeters)
}
