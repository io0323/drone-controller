package com.io.dronecontroller.ui.login

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockLoginViewModel : LoginViewModelContract {
    private val mutableState = MutableStateFlow(LoginUiState())
    override val uiState: StateFlow<LoginUiState> = mutableState

    override fun updateUsername(username: String) {
        mutableState.value = mutableState.value.copy(username = username)
    }

    override fun updatePassword(password: String) {
        mutableState.value = mutableState.value.copy(password = password)
    }

    override fun login() {
        mutableState.value = mutableState.value.copy(isLoggedIn = true, errorMessage = null)
    }
}

