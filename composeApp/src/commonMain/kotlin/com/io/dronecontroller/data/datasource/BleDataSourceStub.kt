package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.model.BleDevice
import com.io.dronecontroller.domain.model.RunStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** iOS / JVM 向けスタブ（BLE非対応プラットフォーム） */
class BleDataSourceStub : BleDataSourceContract {
    override fun scanDevices(): Flow<List<BleDevice>> = flowOf(emptyList())

    override suspend fun connect(address: String): RunStatus<Unit> = RunStatus.Error("BLE非対応プラットフォーム")

    override fun disconnect() = Unit

    override fun observeConnectionStatus(): Flow<BleConnectionStatus> = flowOf(BleConnectionStatus.Disconnected)

    override fun observeControllerState(): Flow<BleControllerState> = flowOf(BleControllerState())
}
