package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.DroneState
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract
import kotlinx.coroutines.flow.Flow

class ObserveDroneStateUseCase(
    private val repository: MavlinkRepositoryContract
) : ObserveDroneStateUseCaseContract {
    override operator fun invoke(address: String, port: Int): Flow<DroneState> =
        repository.observeDroneState(address, port)
}
