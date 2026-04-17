package com.io.dronecontroller.ui.connection

import com.io.dronecontroller.domain.model.ConnectionStatus

data class ConnectionUiState(
    val status: ConnectionStatus = ConnectionStatus.Disconnected,
    val address: String = "192.168.3.11",
    val port: Int = 50051,
    val history: List<Pair<String, Int>> = emptyList(),
    val isDirectUdpMode: Boolean = false,
)
