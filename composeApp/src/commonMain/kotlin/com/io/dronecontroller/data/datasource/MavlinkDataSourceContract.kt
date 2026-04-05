package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

interface MavlinkDataSourceContract {
    fun observeConnectionState(address: String, port: Int): Flow<ConnectionStatus>
    fun disconnect()
}
