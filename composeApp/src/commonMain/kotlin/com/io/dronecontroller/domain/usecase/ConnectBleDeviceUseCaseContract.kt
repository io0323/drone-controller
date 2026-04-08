package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus

interface ConnectBleDeviceUseCaseContract {
    suspend operator fun invoke(address: String): RunStatus<Unit>
}
