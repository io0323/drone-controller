package com.io.dronecontroller.domain.repository

import com.io.dronecontroller.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

interface MavlinkRepositoryContract {
    fun observeConnection(address: String, port: Int): Flow<ConnectionStatus>
    fun disconnect()
}
