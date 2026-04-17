package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.DroneState
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.service.ConnectionModeHolder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
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

// ─── UDP直接接続実装 ───────────────────────────────────────────────

class UdpMavlinkDataSource : MavlinkDataSourceContract {

    override fun observeConnectionState(address: String, port: Int): Flow<ConnectionStatus> =
        callbackFlow {
            val socket = DatagramSocket()
            socket.soTimeout = 3000
            val target = InetAddress.getByName(address)
            var lastHeartbeat = 0L
            var seq = 0

            val sendJob = launch {
                while (isActive) {
                    runCatching {
                        val pkt = buildGcsHeartbeat(seq++ and 0xFF)
                        socket.send(DatagramPacket(pkt, pkt.size, target, port))
                    }
                    delay(1000)
                }
            }

            val receiveJob = launch {
                val buf = ByteArray(512)
                val dp = DatagramPacket(buf, buf.size)
                while (isActive) {
                    try {
                        socket.receive(dp)
                        if (parseMsgId(buf, dp.length) == 0) {
                            lastHeartbeat = System.currentTimeMillis()
                            trySend(ConnectionStatus.Connected(lastHeartbeat))
                        }
                    } catch (_: java.net.SocketTimeoutException) {
                        if (System.currentTimeMillis() - lastHeartbeat > 3000) {
                            trySend(ConnectionStatus.Disconnected)
                        }
                    } catch (e: Exception) {
                        trySend(ConnectionStatus.Error(e.message ?: "UDP接続エラー"))
                    }
                }
            }

            awaitClose {
                sendJob.cancel()
                receiveJob.cancel()
                socket.close()
            }
        }

    override fun observeDroneState(address: String, port: Int): Flow<DroneState> =
        callbackFlow {
            val socket = DatagramSocket()
            socket.soTimeout = 3000
            val target = InetAddress.getByName(address)
            var seq = 0
            var state = DroneState()

            val sendJob = launch {
                while (isActive) {
                    runCatching {
                        val pkt = buildGcsHeartbeat(seq++ and 0xFF)
                        socket.send(DatagramPacket(pkt, pkt.size, target, port))
                    }
                    delay(1000)
                }
            }

            val receiveJob = launch {
                val buf = ByteArray(512)
                val dp = DatagramPacket(buf, buf.size)
                while (isActive) {
                    try {
                        socket.receive(dp)
                        val msgId = parseMsgId(buf, dp.length)
                        val payloadOffset = when (buf[0]) {
                            0xFE.toByte() -> 6
                            0xFD.toByte() -> 10
                            else -> continue
                        }
                        when (msgId) {
                            33 -> { // GLOBAL_POSITION_INT
                                val bb = ByteBuffer.wrap(buf, payloadOffset, dp.length - payloadOffset - 2)
                                    .order(ByteOrder.LITTLE_ENDIAN)
                                bb.int // time_boot_ms
                                val lat = bb.int / 1e7
                                val lon = bb.int / 1e7
                                bb.int // alt MSL mm
                                val relAlt = bb.int / 1000f
                                state = state.copy(latitude = lat, longitude = lon, altitudeMeters = relAlt)
                                trySend(state)
                            }
                        }
                    } catch (_: java.net.SocketTimeoutException) {
                    } catch (_: Exception) {
                    }
                }
            }

            awaitClose {
                sendJob.cancel()
                receiveJob.cancel()
                socket.close()
            }
        }

    override fun disconnect() {}

    override suspend fun takeoff(altitudeMeters: Float): RunStatus<Unit> = RunStatus.Error("UDP直接モードでは未対応")
    override suspend fun land(): RunStatus<Unit> = RunStatus.Error("UDP直接モードでは未対応")
    override suspend fun returnToLaunch(): RunStatus<Unit> = RunStatus.Error("UDP直接モードでは未対応")
    override fun sendManualControl(pitch: Float, roll: Float, throttle: Float, yaw: Float) {}
    override suspend fun capturePhoto(): RunStatus<Unit> = RunStatus.Error("UDP直接モードでは未対応")
    override suspend fun startVideo(): RunStatus<Unit> = RunStatus.Error("UDP直接モードでは未対応")
    override suspend fun stopVideo(): RunStatus<Unit> = RunStatus.Error("UDP直接モードでは未対応")

    private fun parseMsgId(buf: ByteArray, len: Int): Int {
        if (len < 8) return -1
        return when (buf[0]) {
            0xFE.toByte() -> buf[5].toInt() and 0xFF
            0xFD.toByte() -> if (len < 12) -1
                else (buf[7].toInt() and 0xFF) or ((buf[8].toInt() and 0xFF) shl 8) or ((buf[9].toInt() and 0xFF) shl 16)
            else -> -1
        }
    }

    private fun buildGcsHeartbeat(seq: Int): ByteArray {
        val payload = byteArrayOf(0, 0, 0, 0, 6, 8, 0, 4, 3)
        val header = byteArrayOf(0xFE.toByte(), 9, seq.toByte(), 0xFF.toByte(), 0xBE.toByte(), 0)
        val crc = mavlinkCrc(header, payload, 50)
        return header + payload + byteArrayOf((crc and 0xFF).toByte(), ((crc ushr 8) and 0xFF).toByte())
    }

    private fun mavlinkCrc(header: ByteArray, payload: ByteArray, extra: Int): Int {
        var crc = 0xFFFF
        for (b in header.copyOfRange(1, 6) + payload) {
            var tmp = (b.toInt() and 0xFF) xor (crc and 0xFF)
            tmp = tmp xor ((tmp shl 4) and 0xFF)
            crc = ((crc ushr 8) and 0xFF) xor (tmp shl 8) xor (tmp shl 3) xor (tmp ushr 4)
            crc = crc and 0xFFFF
        }
        var tmp = extra xor (crc and 0xFF)
        tmp = tmp xor ((tmp shl 4) and 0xFF)
        crc = ((crc ushr 8) and 0xFF) xor (tmp shl 8) xor (tmp shl 3) xor (tmp ushr 4)
        return crc and 0xFFFF
    }
}

// ─── 接続モードに応じてgRPC/UDPを切り替えるラッパー ─────────────────────

class RoutingMavlinkDataSource(
    private val grpc: MavlinkDataSource,
    private val udp: UdpMavlinkDataSource,
    private val modeHolder: ConnectionModeHolder,
) : MavlinkDataSourceContract {

    private val active: MavlinkDataSourceContract get() = if (modeHolder.isDirectUdpMode) udp else grpc

    override fun observeConnectionState(address: String, port: Int) = active.observeConnectionState(address, port)
    override fun observeDroneState(address: String, port: Int) = active.observeDroneState(address, port)
    override fun disconnect() = active.disconnect()
    override suspend fun takeoff(altitudeMeters: Float) = active.takeoff(altitudeMeters)
    override suspend fun land() = active.land()
    override suspend fun returnToLaunch() = active.returnToLaunch()
    override fun sendManualControl(pitch: Float, roll: Float, throttle: Float, yaw: Float) =
        active.sendManualControl(pitch, roll, throttle, yaw)
    override suspend fun capturePhoto() = active.capturePhoto()
    override suspend fun startVideo() = active.startVideo()
    override suspend fun stopVideo() = active.stopVideo()
}
