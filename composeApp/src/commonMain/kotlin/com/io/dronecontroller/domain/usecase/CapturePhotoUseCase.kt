package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract

class CapturePhotoUseCase(
    private val repository: MavlinkRepositoryContract,
) : CapturePhotoUseCaseContract {
    override suspend operator fun invoke(): RunStatus<Unit> = repository.capturePhoto()
}
