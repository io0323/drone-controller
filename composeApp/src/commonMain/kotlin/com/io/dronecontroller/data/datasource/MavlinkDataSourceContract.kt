package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.DroneState
import com.io.dronecontroller.domain.model.RunStatus
import kotlinx.coroutines.flow.Flow

interface MavlinkDataSourceContract {
    fun observeConnectionState(
        address: String,
        port: Int,
    ): Flow<ConnectionStatus>

    fun observeDroneState(
        address: String,
        port: Int,
    ): Flow<DroneState>

    fun disconnect()

    suspend fun takeoff(altitudeMeters: Float): RunStatus<Unit>

    suspend fun land(): RunStatus<Unit>

    suspend fun returnToLaunch(): RunStatus<Unit>

    fun sendManualControl(
        pitch: Float,
        roll: Float,
        throttle: Float,
        yaw: Float,
    )
}
