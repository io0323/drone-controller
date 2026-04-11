package com.io.dronecontroller.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.usecase.LoginUseCaseContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCaseContract,
) : ViewModel(), LoginViewModelContract {

    private val _uiState = MutableStateFlow(LoginUiState())
    override val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    override fun updateUsername(username: String) {
        _uiState.update { current ->
            current.copy(username = username, errorMessage = null)
        }
    }

    override fun updatePassword(password: String) {
        _uiState.update { current ->
            current.copy(password = password, errorMessage = null)
        }
    }

    override fun login() {
        val current = _uiState.value
        if (current.username.isBlank() || current.password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "ユーザー名とパスワードを入力してください")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = loginUseCase(current.username, current.password)) {
                is RunStatus.Success -> _uiState.update {
                    it.copy(isLoading = false, isLoggedIn = true)
                }

                is RunStatus.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message, isLoggedIn = false)
                }

                is RunStatus.Loading -> Unit
            }
        }
    }
}

