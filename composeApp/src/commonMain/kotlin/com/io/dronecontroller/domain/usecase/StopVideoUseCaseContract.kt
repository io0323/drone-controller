package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus

interface StopVideoUseCaseContract {
    suspend operator fun invoke(): RunStatus<Unit>
}
