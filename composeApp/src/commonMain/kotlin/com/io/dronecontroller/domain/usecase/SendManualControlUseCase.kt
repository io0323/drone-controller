package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract

class SendManualControlUseCase(
    private val repository: MavlinkRepositoryContract,
) : SendManualControlUseCaseContract {
    override operator fun invoke(
        pitch: Float,
        roll: Float,
        throttle: Float,
        yaw: Float,
    ) = repository.sendManualControl(pitch, roll, throttle, yaw)
}
