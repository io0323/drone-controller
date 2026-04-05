package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.ConnectionStatus
import io.mavsdk.System as MavsdkSystem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

    override fun disconnect() {
        drone?.dispose()
        drone = null
    }
}
