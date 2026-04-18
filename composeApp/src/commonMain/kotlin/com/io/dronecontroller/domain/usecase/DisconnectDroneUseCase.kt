package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract

class DisconnectDroneUseCase(
    private val repository: MavlinkRepositoryContract,
) : DisconnectDroneUseCaseContract {
    override operator fun invoke() = repository.disconnect()
}
