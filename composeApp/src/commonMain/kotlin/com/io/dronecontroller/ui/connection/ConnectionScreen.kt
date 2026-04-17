package com.io.dronecontroller.ui.connection

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.ui.controller.CameraBackground
import org.koin.compose.viewmodel.koinViewModel

private val CardBg = Color(0xCC0D1520)
private val GreenAccent = Color(0xFF4ADE80)
private val BlueAccent = Color(0xFF60A5FA)

@Composable
fun ConnectionScreen(
    onConnected: () -> Unit,
    onMock: () -> Unit = {},
    viewModel: ConnectionViewModelContract = koinViewModel<ConnectionViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.status) {
        if (uiState.status is ConnectionStatus.Connected) onConnected()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CameraBackground()

        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xAA000000)),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth(0.88f)
                    .background(CardBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DroneIconSection(status = uiState.status)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = !uiState.isDirectUdpMode,
                    onClick = { if (uiState.isDirectUdpMode) viewModel.toggleMode() },
                    enabled = uiState.status !is ConnectionStatus.Connected,
                    colors = RadioButtonDefaults.colors(selectedColor = BlueAccent),
                )
                Text("本番 (gRPC)", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                RadioButton(
                    selected = uiState.isDirectUdpMode,
                    onClick = { if (!uiState.isDirectUdpMode) viewModel.toggleMode() },
                    enabled = uiState.status !is ConnectionStatus.Connected,
                    colors = RadioButtonDefaults.colors(selectedColor = BlueAccent),
                )
                Text("モック (UDP直接)", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
            }

            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::updateAddress,
                label = {
                    Text(
                        if (uiState.isDirectUdpMode) "drone-emulatorのIPアドレス" else "mavsdk_serverのIPアドレス",
                        color = Color.Gray,
                        fontSize = 12.sp,
                    )
                },
                singleLine = true,
                enabled = uiState.status !is ConnectionStatus.Connected,
                colors = inputColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.port.toString(),
                onValueChange = { viewModel.updatePort(it.toIntOrNull() ?: if (uiState.isDirectUdpMode) 14550 else 50051) },
                label = {
                    Text(
                        if (uiState.isDirectUdpMode) "UDPポート（デフォルト: 14550）" else "gRPCポート（デフォルト: 50051）",
                        color = Color.Gray,
                        fontSize = 12.sp,
                    )
                },
                singleLine = true,
                enabled = uiState.status !is ConnectionStatus.Connected,
                colors = inputColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            if (uiState.history.isNotEmpty()) {
                HistorySection(
                    history = uiState.history,
                    onSelect = { address, port -> viewModel.selectHistory(address, port) },
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = viewModel::connect,
                    enabled = uiState.status is ConnectionStatus.Disconnected || uiState.status is ConnectionStatus.Error,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    modifier = Modifier.weight(1f),
                ) { Text("接続", color = Color.White) }

                Button(
                    onClick = viewModel::disconnect,
                    enabled = uiState.status is ConnectionStatus.Connecting || uiState.status is ConnectionStatus.Connected,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                    modifier = Modifier.weight(1f),
                ) { Text("切断", color = Color.White) }

                Button(
                    onClick = {
                        viewModel.mock()
                        onMock()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                    modifier = Modifier.weight(1f),
                ) { Text("モック", color = Color.White) }
            }

            StatusText(status = uiState.status)
        }
    }
}

@Composable
private fun DroneIconSection(status: ConnectionStatus) {
    val isConnecting = status is ConnectionStatus.Connecting
    val isConnected = status is ConnectionStatus.Connected

    val infiniteTransition = rememberInfiniteTransition()
    val animScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
    )
    val animAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
    )

    val iconScale = if (isConnecting) animScale else 1f
    val iconAlpha = if (isConnecting) animAlpha else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(120.dp)
                    .scale(iconScale)
                    .background(
                        color =
                            when {
                                isConnected -> GreenAccent.copy(alpha = 0.15f)
                                isConnecting -> BlueAccent.copy(alpha = 0.12f)
                                else -> Color(0xFF1A2233)
                            },
                        shape = CircleShape,
                    ),
        ) {
            Icon(
                imageVector = Icons.Default.Flight,
                contentDescription = "ドローン",
                modifier = Modifier.size(68.dp).alpha(iconAlpha),
                tint =
                    when {
                        isConnected -> GreenAccent
                        isConnecting -> BlueAccent
                        else -> Color(0xFF4B5563)
                    },
            )
        }
        Text(
            text = "ドローン接続",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun HistorySection(
    history: List<Pair<String, Int>>,
    onSelect: (String, Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp),
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Text("接続履歴", color = Color.Gray, fontSize = 12.sp)
            }
            history.forEach { (address, port) ->
                Text(
                    text = "$address : $port",
                    color = BlueAccent,
                    fontSize = 13.sp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(address, port) }
                            .padding(vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusText(status: ConnectionStatus) {
    val (text, color) =
        when (status) {
            is ConnectionStatus.Disconnected -> "未接続" to Color.Gray
            is ConnectionStatus.Connecting -> "接続中..." to BlueAccent
            is ConnectionStatus.Connected -> "接続済み" to GreenAccent
            is ConnectionStatus.Error -> "エラー: ${status.message}" to Color(0xFFEF4444)
        }
    Text(text = text, color = color, fontSize = 13.sp)
}

@Composable
private fun inputColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = BlueAccent,
        unfocusedBorderColor = Color(0xFF374151),
    )
