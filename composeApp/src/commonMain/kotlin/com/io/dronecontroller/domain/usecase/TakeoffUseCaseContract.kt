package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus

interface TakeoffUseCaseContract {
    suspend operator fun invoke(altitudeMeters: Float): RunStatus<Unit>
}
