package com.io.dronecontroller.ui.connection

import kotlinx.coroutines.flow.StateFlow

interface ConnectionViewModelContract {
    val uiState: StateFlow<ConnectionUiState>

    fun connect()

    fun disconnect()

    fun mock()

    fun updateAddress(address: String)

    fun updatePort(port: Int)
}
