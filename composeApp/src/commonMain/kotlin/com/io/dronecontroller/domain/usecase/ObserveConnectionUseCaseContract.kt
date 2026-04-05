package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

interface ObserveConnectionUseCaseContract {
    operator fun invoke(address: String, port: Int): Flow<ConnectionStatus>
}
