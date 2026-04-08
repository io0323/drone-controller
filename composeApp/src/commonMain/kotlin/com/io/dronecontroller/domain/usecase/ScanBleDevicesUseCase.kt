package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.BleDevice
import com.io.dronecontroller.domain.repository.BleRepositoryContract
import kotlinx.coroutines.flow.Flow

class ScanBleDevicesUseCase(
    private val repository: BleRepositoryContract
) : ScanBleDevicesUseCaseContract {
    override operator fun invoke(): Flow<List<BleDevice>> = repository.scanDevices()
}
