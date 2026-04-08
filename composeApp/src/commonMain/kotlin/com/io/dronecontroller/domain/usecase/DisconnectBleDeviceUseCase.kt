package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.repository.BleRepositoryContract

class DisconnectBleDeviceUseCase(
    private val repository: BleRepositoryContract
) : DisconnectBleDeviceUseCaseContract {
    override operator fun invoke() = repository.disconnect()
}
