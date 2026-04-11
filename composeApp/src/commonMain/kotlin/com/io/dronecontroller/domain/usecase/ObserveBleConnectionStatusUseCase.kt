package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.repository.BleRepositoryContract
import kotlinx.coroutines.flow.Flow

class ObserveBleConnectionStatusUseCase(
    private val repository: BleRepositoryContract,
) : ObserveBleConnectionStatusUseCaseContract {
    override operator fun invoke(): Flow<BleConnectionStatus> = repository.observeConnectionStatus()
}
