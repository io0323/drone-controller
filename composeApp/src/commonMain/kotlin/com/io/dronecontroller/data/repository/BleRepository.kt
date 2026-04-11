package com.io.dronecontroller.data.repository

import com.io.dronecontroller.data.datasource.BleDataSourceContract
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.model.BleDevice
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.BleRepositoryContract
import kotlinx.coroutines.flow.Flow

class BleRepository(
    private val dataSource: BleDataSourceContract,
) : BleRepositoryContract {
    override fun scanDevices(): Flow<List<BleDevice>> = dataSource.scanDevices()

    override suspend fun connect(address: String): RunStatus<Unit> = dataSource.connect(address)

    override fun disconnect() = dataSource.disconnect()

    override fun observeConnectionStatus(): Flow<BleConnectionStatus> = dataSource.observeConnectionStatus()

    override fun observeControllerState(): Flow<BleControllerState> = dataSource.observeControllerState()
}
