package com.io.dronecontroller.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.usecase.ObserveConnectionUseCaseContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val observeConnection: ObserveConnectionUseCaseContract,
) : ViewModel(),
    ConnectionViewModelContract {
    private val _uiState = MutableStateFlow(ConnectionUiState())
    override val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    private var connectionJob: Job? = null

    override fun connect() {
        val current = _uiState.value
        connectionJob?.cancel()
        _uiState.update { it.copy(status = ConnectionStatus.Connecting) }
        connectionJob =
            viewModelScope.launch {
                observeConnection(current.address, current.port)
                    .collect { status ->
                        _uiState.update { it.copy(status = status) }
                    }
            }
    }

    override fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        _uiState.update { it.copy(status = ConnectionStatus.Disconnected) }
    }

    override fun mock() {
        connectionJob?.cancel()
        connectionJob = null
        _uiState.update { it.copy(status = ConnectionStatus.Disconnected) }
    }

    override fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    override fun updatePort(port: Int) {
        _uiState.update { it.copy(port = port) }
    }
}
