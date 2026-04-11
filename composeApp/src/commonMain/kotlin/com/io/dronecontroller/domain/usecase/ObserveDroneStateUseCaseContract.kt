package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.DroneState
import kotlinx.coroutines.flow.Flow

interface ObserveDroneStateUseCaseContract {
    operator fun invoke(
        address: String,
        port: Int,
    ): Flow<DroneState>
}
