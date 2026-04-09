package com.io.dronecontroller.fake

import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.DroneState
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeMavlinkRepository : MavlinkRepositoryContract {

    var takeoffResult: RunStatus<Unit> = RunStatus.Success(Unit)
    var landResult: RunStatus<Unit> = RunStatus.Success(Unit)
    var returnToLaunchResult: RunStatus<Unit> = RunStatus.Success(Unit)

    val droneStateFlow = MutableSharedFlow<DroneState>(replay = 1)
    val connectionFlow = MutableSharedFlow<ConnectionStatus>()

    var disconnectCalled = false

    // ★ 追加（重要）
    var lastTakeoffAltitude: Float? = null

    var lastManualControlPitch: Float? = null
    var lastManualControlRoll: Float? = null
    var lastManualControlThrottle: Float? = null
    var lastManualControlYaw: Float? = null

    override fun observeConnection(address: String, port: Int): Flow<ConnectionStatus> =
        connectionFlow

    override fun observeDroneState(address: String, port: Int): Flow<DroneState> =
        droneStateFlow

    override fun disconnect() {
        disconnectCalled = true
    }

    override suspend fun takeoff(altitudeMeters: Float): RunStatus<Unit> {
        // ★ 呼び出し記録
        lastTakeoffAltitude = altitudeMeters
        return takeoffResult
    }

    override suspend fun land(): RunStatus<Unit> =
        landResult

    override suspend fun returnToLaunch(): RunStatus<Unit> =
        returnToLaunchResult

    override fun sendManualControl(
        pitch: Float,
        roll: Float,
        throttle: Float,
        yaw: Float
    ) {
        lastManualControlPitch = pitch
        lastManualControlRoll = roll
        lastManualControlThrottle = throttle
        lastManualControlYaw = yaw
    }
}