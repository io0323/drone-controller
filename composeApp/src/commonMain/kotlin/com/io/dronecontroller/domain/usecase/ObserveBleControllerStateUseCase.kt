package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.repository.BleRepositoryContract
import kotlinx.coroutines.flow.Flow

class ObserveBleControllerStateUseCase(
    private val repository: BleRepositoryContract
) : ObserveBleControllerStateUseCaseContract {
    override operator fun invoke(): Flow<BleControllerState> = repository.observeControllerState()
}
