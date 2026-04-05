package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract
import kotlinx.coroutines.flow.Flow

class ObserveConnectionUseCase(
    private val repository: MavlinkRepositoryContract
) : ObserveConnectionUseCaseContract {
    override operator fun invoke(address: String, port: Int): Flow<ConnectionStatus> =
        repository.observeConnection(address, port)
}
