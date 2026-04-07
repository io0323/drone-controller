package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus

interface LandUseCaseContract {
    suspend operator fun invoke(): RunStatus<Unit>
}
