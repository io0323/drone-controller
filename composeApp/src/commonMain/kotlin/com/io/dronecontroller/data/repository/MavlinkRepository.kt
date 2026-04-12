package com.io.dronecontroller.data.repository

import com.io.dronecontroller.data.datasource.MavlinkDataSourceContract
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.DroneState
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract
import kotlinx.coroutines.flow.Flow

class MavlinkRepository(
    private val dataSource: MavlinkDataSourceContract,
) : MavlinkRepositoryContract {
    override fun observeConnection(
        address: String,
        port: Int,
    ): Flow<ConnectionStatus> = dataSource.observeConnectionState(address, port)

    override fun observeDroneState(
        address: String,
        port: Int,
    ): Flow<DroneState> = dataSource.observeDroneState(address, port)

    override fun disconnect() = dataSource.disconnect()

    override suspend fun takeoff(altitudeMeters: Float): RunStatus<Unit> = dataSource.takeoff(altitudeMeters)

    override suspend fun land(): RunStatus<Unit> = dataSource.land()

    override suspend fun returnToLaunch(): RunStatus<Unit> = dataSource.returnToLaunch()

    override fun sendManualControl(
        pitch: Float,
        roll: Float,
        throttle: Float,
        yaw: Float,
    ) = dataSource.sendManualControl(pitch, roll, throttle, yaw)

    override suspend fun capturePhoto(): RunStatus<Unit> = dataSource.capturePhoto()

    override suspend fun startVideo(): RunStatus<Unit> = dataSource.startVideo()

    override suspend fun stopVideo(): RunStatus<Unit> = dataSource.stopVideo()
}
