package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.BleConnectionStatus
import kotlinx.coroutines.flow.Flow

interface ObserveBleConnectionStatusUseCaseContract {
    operator fun invoke(): Flow<BleConnectionStatus>
}
