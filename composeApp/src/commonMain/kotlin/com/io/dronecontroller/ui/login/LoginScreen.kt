package com.io.dronecontroller.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModelContract = koinViewModel<LoginViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoggedIn()
        }
    }

    LoginContent(
        uiState = uiState,
        onUsernameChange = viewModel::updateUsername,
        onPasswordChange = viewModel::updatePassword,
        onLogin = viewModel::login,
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "モックログイン",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "デモ資格情報: demo / demo123",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChange,
            label = { Text("ユーザー名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("パスワード") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            enabled = !uiState.isLoading,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogin,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("ログイン")
            }
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onLoggedIn = {},
            viewModel = MockLoginViewModel(),
        )
    }
}

