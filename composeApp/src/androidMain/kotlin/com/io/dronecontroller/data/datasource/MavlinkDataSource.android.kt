package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.DroneState
import com.io.dronecontroller.domain.model.RunStatus
import io.mavsdk.System as MavsdkSystem
import kotlin.math.sqrt
import kotlin.coroutines.resume
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

class MavlinkDataSource : MavlinkDataSourceContract {

    private var drone: MavsdkSystem? = null

    override fun observeConnectionState(address: String, port: Int): Flow<ConnectionStatus> = callbackFlow {
        val system = MavsdkSystem(address, port).also { drone = it }

        val disposable = system.core.connectionState.subscribe(
            { state ->
                val status = if (state.isConnected == true) {
                    ConnectionStatus.Connected(
                        lastHeartbeatAt = System.currentTimeMillis()
                    )
                } else {
                    ConnectionStatus.Disconnected
                }
                trySend(status)
            },
            { error ->
                trySend(ConnectionStatus.Error(error.message ?: "接続エラー"))
                close(error)
            }
        )

        awaitClose {
            disposable.dispose()
            system.dispose()
            drone = null
        }
    }

    override fun observeDroneState(address: String, port: Int): Flow<DroneState> = callbackFlow {
        val system = MavsdkSystem(address, port).also { drone = it }
        var altitudeMeters = 0f
        var batteryPercent = 0
        var speedKmh = 0f
        var satelliteCount = 0
        var connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected
        var isArmed = false
        var latitude = 0.0
        var longitude = 0.0
        var bearing = 0f

        fun sendCurrent() {
            trySend(
                DroneState(
                    altitudeMeters = altitudeMeters,
                    batteryPercent = batteryPercent,
                    speedKmh = speedKmh,
                    satelliteCount = satelliteCount,
                    connectionStatus = connectionStatus,
                    isArmed = isArmed,
                    latitude = latitude,
                    longitude = longitude,
                    bearing = bearing
                )
            )
        }

        val disposables = listOf(
            system.core.connectionState.subscribe(
                { state ->
                    connectionStatus = if (state.isConnected == true) {
                        ConnectionStatus.Connected(System.currentTimeMillis())
                    } else {
                        ConnectionStatus.Disconnected
                    }
                    sendCurrent()
                },
                { }
            ),
            system.telemetry.position.subscribe(
                { pos ->
                    altitudeMeters = pos.relativeAltitudeM
                    latitude = pos.latitudeDeg
                    longitude = pos.longitudeDeg
                    sendCurrent()
                },
                { }
            ),
            system.telemetry.battery.subscribe(
                { bat ->
                    batteryPercent = (bat.remainingPercent * 100).toInt().coerceIn(0, 100)
                    sendCurrent()
                },
                { }
            ),
            system.telemetry.velocityNed.subscribe(
                { vel ->
                    speedKmh = sqrt(vel.northMS * vel.northMS + vel.eastMS * vel.eastMS) * 3.6f
                    sendCurrent()
                },
                { }
            ),
            system.telemetry.gpsInfo.subscribe(
                { gps ->
                    satelliteCount = gps.numSatellites
                    sendCurrent()
                },
                { }
            ),
            system.telemetry.armed.subscribe(
                { armed ->
                    isArmed = armed
                    sendCurrent()
                },
                { }
            ),
            system.telemetry.heading.subscribe(
                { heading ->
                    bearing = heading.headingDeg.toFloat()
                    sendCurrent()
                },
                { }
            )
        )

        awaitClose {
            disposables.forEach { it.dispose() }
            system.dispose()
        }
    }

    override fun disconnect() {
        drone?.dispose()
        drone = null
    }

    override suspend fun takeoff(altitudeMeters: Float): RunStatus<Unit> {
        val system = drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable = system.action.takeoff().subscribe(
                { cont.resume(RunStatus.Success(Unit)) },
                { e -> cont.resume(RunStatus.Error(e.message ?: "離陸コマンド失敗", e)) }
            )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    override suspend fun land(): RunStatus<Unit> {
        val system = drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable = system.action.land().subscribe(
                { cont.resume(RunStatus.Success(Unit)) },
                { e -> cont.resume(RunStatus.Error(e.message ?: "着陸コマンド失敗", e)) }
            )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    override suspend fun returnToLaunch(): RunStatus<Unit> {
        val system = drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable = system.action.returnToLaunch().subscribe(
                { cont.resume(RunStatus.Success(Unit)) },
                { e -> cont.resume(RunStatus.Error(e.message ?: "RTLコマンド失敗", e)) }
            )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    override fun sendManualControl(pitch: Float, roll: Float, throttle: Float, yaw: Float) {
        // TODO: io.mavsdk バージョンに合わせて実装
        // MAVSDK ManualControl plugin の API シグネチャを確認後に有効化:
        // system.manualControl.setManualControl(roll, pitch, throttle, yaw, 0).subscribe({}, {})
    }
}
