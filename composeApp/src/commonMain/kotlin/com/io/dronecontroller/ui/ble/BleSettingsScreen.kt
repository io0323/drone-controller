package com.io.dronecontroller.ui.ble

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleDevice
import org.koin.compose.viewmodel.koinViewModel

private val PanelBg = Color(0xF0121212)
private val GreenAccent = Color(0xFF4ADE80)
private val BlueAccent = Color(0xFF60A5FA)
private val DividerColor = Color(0xFF2A2A2A)

@Composable
fun BleSettingsScreen(
    viewModel: BleControllerViewModelContract = koinViewModel<BleControllerViewModel>(),
    onDismiss: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // 背景タップで閉じる
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {}) // 内部クリックは伝播させない
                .padding(horizontal = 0.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = PanelBg
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ヘッダー
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BLEコントローラー設定",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 接続状態
                ConnectionStatusRow(status = uiState.connectionStatus)

                // エラーメッセージ
                uiState.errorMessage?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = Color(0xFFFC6868),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(Modifier.height(12.dp))

                // スキャン操作行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isScanning) "スキャン中..." else "近くのデバイスを検索",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BlueAccent,
                                strokeWidth = 2.dp
                            )
                        }
                        if (uiState.connectionStatus is BleConnectionStatus.Connected) {
                            Button(
                                onClick = { viewModel.disconnect() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFC6868).copy(alpha = 0.8f)
                                )
                            ) { Text("切断", fontSize = 12.sp) }
                        } else {
                            Button(
                                onClick = {
                                    if (uiState.isScanning) viewModel.stopScan()
                                    else viewModel.startScan()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BlueAccent.copy(alpha = 0.85f)
                                )
                            ) {
                                Text(
                                    text = if (uiState.isScanning) "停止" else "スキャン",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // デバイスリスト
                if (uiState.scannedDevices.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.scannedDevices) { device ->
                            DeviceRow(
                                device = device,
                                isConnected = (uiState.connectionStatus as? BleConnectionStatus.Connected)
                                    ?.device?.address == device.address,
                                onConnect = { viewModel.connect(device.address) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ConnectionStatusRow(status: BleConnectionStatus) {
    val (icon, label, tint) = when (status) {
        is BleConnectionStatus.Connected ->
            Triple(Icons.Default.BluetoothConnected, "接続済み: ${status.device.name}", GreenAccent)
        is BleConnectionStatus.Connecting ->
            Triple(Icons.Default.Bluetooth, "接続中: ${status.device.name}", BlueAccent)
        is BleConnectionStatus.Scanning ->
            Triple(Icons.Default.Bluetooth, "スキャン中", BlueAccent)
        is BleConnectionStatus.Error ->
            Triple(Icons.Default.BluetoothDisabled, "エラー: ${status.message}", Color(0xFFFC6868))
        BleConnectionStatus.Disconnected ->
            Triple(Icons.Default.BluetoothDisabled, "未接続", Color.Gray)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(text = label, color = tint, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DeviceRow(
    device: BleDevice,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isConnected) GreenAccent.copy(alpha = 0.12f) else Color(0xFF1E1E1E)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = device.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = device.address,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }
            if (!isConnected) {
                Button(
                    onClick = onConnect,
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueAccent.copy(alpha = 0.8f)
                    )
                ) {
                    Text("接続", fontSize = 11.sp)
                }
            } else {
                Icon(
                    Icons.Default.BluetoothConnected,
                    contentDescription = "接続済み",
                    tint = GreenAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================
// Preview
// ============================================================

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun BleSettingsScreenPreview() {
    BleSettingsScreen(viewModel = MockBleControllerViewModel())
}
