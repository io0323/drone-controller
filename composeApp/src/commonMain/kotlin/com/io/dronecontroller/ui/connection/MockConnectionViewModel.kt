package com.io.dronecontroller.ui.connection

import com.io.dronecontroller.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockConnectionViewModel : ConnectionViewModelContract {
    override val uiState: StateFlow<ConnectionUiState> =
        MutableStateFlow(
            ConnectionUiState(status = ConnectionStatus.Connected(lastHeartbeatAt = 0L)),
        )

    override fun connect() = Unit

    override fun disconnect() = Unit

    override fun updateAddress(address: String) = Unit

    override fun updatePort(port: Int) = Unit
}
