package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.DroneState
import com.io.dronecontroller.domain.model.RunStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.sqrt
import io.mavsdk.System as MavsdkSystem

private const val RECONNECT_DELAY_MS = 5_000L
private const val MAX_RETRY_COUNT = 3

class MavlinkDataSource(
    private val droneProvider: DroneProvider,
) : MavlinkDataSourceContract {
    override fun observeConnectionState(
        address: String,
        port: Int,
    ): Flow<ConnectionStatus> =
        callbackFlow {
            val system = MavsdkSystem(address, port).also { droneProvider.drone = it }

            val disposable =
                system.core.connectionState.subscribe(
                    { state ->
                        val status =
                            if (state.isConnected == true) {
                                ConnectionStatus.Connected(lastHeartbeatAt = System.currentTimeMillis())
                            } else {
                                ConnectionStatus.Disconnected
                            }
                        trySend(status)
                    },
                    { error ->
                        trySend(ConnectionStatus.Error(error.message ?: "接続エラー"))
                        close(error)
                    },
                )

            awaitClose {
                disposable.dispose()
                system.dispose()
                droneProvider.drone = null
            }
        }

    override fun observeDroneState(
        address: String,
        port: Int,
    ): Flow<DroneState> =
        flow {
            emitAll(observeDroneStateInternal(address, port))
        }.retryWhen { _, _ ->
            // 接続断後に自動再接続（5秒待機）
            delay(RECONNECT_DELAY_MS)
            true
        }

    private fun observeDroneStateInternal(
        address: String,
        port: Int,
    ): Flow<DroneState> =
        callbackFlow {
            val system = MavsdkSystem(address, port).also { droneProvider.drone = it }
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
                        bearing = bearing,
                    ),
                )
            }

            val disposables =
                listOf(
                    system.core.connectionState.subscribe(
                        { state ->
                            connectionStatus =
                                if (state.isConnected == true) {
                                    ConnectionStatus.Connected(System.currentTimeMillis())
                                } else {
                                    ConnectionStatus.Disconnected
                                }
                            sendCurrent()
                        },
                        { },
                    ),
                    system.telemetry.position.subscribe(
                        { pos ->
                            altitudeMeters = pos.relativeAltitudeM
                            latitude = pos.latitudeDeg
                            longitude = pos.longitudeDeg
                            sendCurrent()
                        },
                        { },
                    ),
                    system.telemetry.battery.subscribe(
                        { bat ->
                            batteryPercent = (bat.remainingPercent * 100).toInt().coerceIn(0, 100)
                            sendCurrent()
                        },
                        { },
                    ),
                    system.telemetry.velocityNed.subscribe(
                        { vel ->
                            speedKmh = sqrt(vel.northMS * vel.northMS + vel.eastMS * vel.eastMS) * 3.6f
                            sendCurrent()
                        },
                        { },
                    ),
                    system.telemetry.gpsInfo.subscribe(
                        { gps ->
                            satelliteCount = gps.numSatellites
                            sendCurrent()
                        },
                        { },
                    ),
                    system.telemetry.armed.subscribe(
                        { armed ->
                            isArmed = armed
                            sendCurrent()
                        },
                        { },
                    ),
                    system.telemetry.heading.subscribe(
                        { heading ->
                            bearing = heading.headingDeg.toFloat()
                            sendCurrent()
                        },
                        { },
                    ),
                )

            awaitClose {
                disposables.forEach { it.dispose() }
                system.dispose()
                droneProvider.drone = null
            }
        }

    override fun disconnect() {
        droneProvider.drone?.dispose()
        droneProvider.drone = null
    }

    override suspend fun takeoff(altitudeMeters: Float): RunStatus<Unit> = withRetry { executeTakeoff(altitudeMeters) }

    override suspend fun land(): RunStatus<Unit> = withRetry { executeLand() }

    override suspend fun returnToLaunch(): RunStatus<Unit> = withRetry { executeReturnToLaunch() }

    override fun sendManualControl(
        pitch: Float,
        roll: Float,
        throttle: Float,
        yaw: Float,
    ) {
        // TODO: io.mavsdk バージョンに合わせて実装
    }

    override suspend fun capturePhoto(): RunStatus<Unit> = withRetry { executeCapturePhoto() }

    override suspend fun startVideo(): RunStatus<Unit> = withRetry { executeStartVideo() }

    override suspend fun stopVideo(): RunStatus<Unit> = withRetry { executeStopVideo() }

    // ─── プライベートヘルパー ─────────────────────────────────────

    private suspend fun executeTakeoff(altitudeMeters: Float): RunStatus<Unit> {
        val system = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable =
                system.action.takeoff().subscribe(
                    { cont.resume(RunStatus.Success(Unit)) },
                    { e -> cont.resume(RunStatus.Error(e.message ?: "離陸コマンド失敗", e)) },
                )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    private suspend fun executeLand(): RunStatus<Unit> {
        val system = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable =
                system.action.land().subscribe(
                    { cont.resume(RunStatus.Success(Unit)) },
                    { e -> cont.resume(RunStatus.Error(e.message ?: "着陸コマンド失敗", e)) },
                )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    private suspend fun executeReturnToLaunch(): RunStatus<Unit> {
        val system = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable =
                system.action.returnToLaunch().subscribe(
                    { cont.resume(RunStatus.Success(Unit)) },
                    { e -> cont.resume(RunStatus.Error(e.message ?: "RTLコマンド失敗", e)) },
                )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    private suspend fun executeCapturePhoto(): RunStatus<Unit> {
        val system = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable =
                system.camera.takePhoto(1).subscribe(
                    { cont.resume(RunStatus.Success(Unit)) },
                    { e -> cont.resume(RunStatus.Error(e.message ?: "撮影コマンド失敗", e)) },
                )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    private suspend fun executeStartVideo(): RunStatus<Unit> {
        val system = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable =
                system.camera.startVideo(1).subscribe(
                    { cont.resume(RunStatus.Success(Unit)) },
                    { e -> cont.resume(RunStatus.Error(e.message ?: "録画開始コマンド失敗", e)) },
                )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    private suspend fun executeStopVideo(): RunStatus<Unit> {
        val system = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable =
                system.camera.stopVideo(1).subscribe(
                    { cont.resume(RunStatus.Success(Unit)) },
                    { e -> cont.resume(RunStatus.Error(e.message ?: "録画停止コマンド失敗", e)) },
                )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    /**
     * 最大 [MAX_RETRY_COUNT] 回リトライするラッパー。
     * 成功したら即返す。最終試行の結果をそのまま返す。
     */
    private suspend fun <T> withRetry(block: suspend () -> RunStatus<T>): RunStatus<T> {
        repeat(MAX_RETRY_COUNT - 1) {
            val result = block()
            if (result is RunStatus.Success) return result
        }
        return block()
    }
}
