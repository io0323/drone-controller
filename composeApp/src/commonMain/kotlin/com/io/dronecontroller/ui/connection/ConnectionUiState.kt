package com.io.dronecontroller.ui.connection

import com.io.dronecontroller.domain.model.ConnectionStatus

data class ConnectionUiState(
    val status: ConnectionStatus = ConnectionStatus.Disconnected,
    val address: String = "10.0.2.2",
    val port: Int = 50051
)
