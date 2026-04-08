package com.io.dronecontroller.domain.repository

import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.model.BleDevice
import com.io.dronecontroller.domain.model.RunStatus
import kotlinx.coroutines.flow.Flow

interface BleRepositoryContract {
    fun scanDevices(): Flow<List<BleDevice>>
    suspend fun connect(address: String): RunStatus<Unit>
    fun disconnect()
    fun observeConnectionStatus(): Flow<BleConnectionStatus>
    fun observeControllerState(): Flow<BleControllerState>
}
