package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.BleRepositoryContract

class ConnectBleDeviceUseCase(
    private val repository: BleRepositoryContract,
) : ConnectBleDeviceUseCaseContract {
    override suspend operator fun invoke(address: String): RunStatus<Unit> = repository.connect(address)
}
