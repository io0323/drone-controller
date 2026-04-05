package com.io.dronecontroller.ui.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.io.dronecontroller.domain.model.ConnectionStatus
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModelContract = koinViewModel<ConnectionViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()
    ConnectionContent(
        uiState = uiState,
        onConnect = viewModel::connect,
        onDisconnect = viewModel::disconnect,
        onAddressChange = viewModel::updateAddress,
        onPortChange = { viewModel.updatePort(it.toIntOrNull() ?: 50051) }
    )
}

@Composable
private fun ConnectionContent(
    uiState: ConnectionUiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onAddressChange: (String) -> Unit,
    onPortChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ドローン接続",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.address,
            onValueChange = onAddressChange,
            label = { Text("アドレス") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.status !is ConnectionStatus.Connected
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.port.toString(),
            onValueChange = onPortChange,
            label = { Text("ポート") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.status !is ConnectionStatus.Connected
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Button(
                onClick = onConnect,
                enabled = uiState.status is ConnectionStatus.Disconnected ||
                    uiState.status is ConnectionStatus.Error
            ) {
                Text("接続")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onDisconnect,
                enabled = uiState.status is ConnectionStatus.Connecting ||
                    uiState.status is ConnectionStatus.Connected
            ) {
                Text("切断")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        StatusDisplay(status = uiState.status)
    }
}

@Composable
private fun StatusDisplay(status: ConnectionStatus) {
    val (label, color) = when (status) {
        is ConnectionStatus.Disconnected -> "未接続" to MaterialTheme.colorScheme.outline
        is ConnectionStatus.Connecting -> "接続中..." to MaterialTheme.colorScheme.primary
        is ConnectionStatus.Connected -> "接続済み (HEARTBEAT受信済み)" to MaterialTheme.colorScheme.tertiary
        is ConnectionStatus.Error -> "エラー: ${status.message}" to MaterialTheme.colorScheme.error
    }
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = color
    )
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ConnectionScreenPreview() {
    MaterialTheme {
        ConnectionContent(
            uiState = ConnectionUiState(
                status = ConnectionStatus.Connected(lastHeartbeatAt = 0L)
            ),
            onConnect = {},
            onDisconnect = {},
            onAddressChange = {},
            onPortChange = {}
        )
    }
}
