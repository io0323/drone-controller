package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface ScanBleDevicesUseCaseContract {
    operator fun invoke(): Flow<List<BleDevice>>
}
