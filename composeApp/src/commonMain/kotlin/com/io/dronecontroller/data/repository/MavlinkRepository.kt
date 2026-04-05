package com.io.dronecontroller.data.repository

import com.io.dronecontroller.data.datasource.MavlinkDataSourceContract
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract
import kotlinx.coroutines.flow.Flow

class MavlinkRepository(
    private val dataSource: MavlinkDataSourceContract
) : MavlinkRepositoryContract {
    override fun observeConnection(address: String, port: Int): Flow<ConnectionStatus> =
        dataSource.observeConnectionState(address, port)

    override fun disconnect() = dataSource.disconnect()
}
