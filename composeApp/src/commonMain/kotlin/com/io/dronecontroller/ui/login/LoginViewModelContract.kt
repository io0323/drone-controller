package com.io.dronecontroller.ui.login

import kotlinx.coroutines.flow.StateFlow

interface LoginViewModelContract {
    val uiState: StateFlow<LoginUiState>
    fun updateUsername(username: String)
    fun updatePassword(password: String)
    fun login()
}

