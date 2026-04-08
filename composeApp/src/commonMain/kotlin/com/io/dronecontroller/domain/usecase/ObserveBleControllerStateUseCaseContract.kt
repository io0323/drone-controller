package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.BleControllerState
import kotlinx.coroutines.flow.Flow

interface ObserveBleControllerStateUseCaseContract {
    operator fun invoke(): Flow<BleControllerState>
}
