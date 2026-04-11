package com.io.dronecontroller.domain.model

sealed class ConnectionStatus {
    object Disconnected : ConnectionStatus()

    object Connecting : ConnectionStatus()

    data class Connected(
        val lastHeartbeatAt: Long,
    ) : ConnectionStatus()

    data class Error(
        val message: String,
    ) : ConnectionStatus()
}
