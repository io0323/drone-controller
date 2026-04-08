package com.io.dronecontroller.ui.mission

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionStatus
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.ui.map.MissionMapView
import org.koin.compose.viewmodel.koinViewModel

private val BgColor = Color(0xFF111827)
private val CardBg = Color(0xFF1F2937)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentGreen = Color(0xFF22C55E)
private val AccentRed = Color(0xFFEF4444)
private val AccentOrange = Color(0xFFF97316)
private val TextPrimary = Color(0xFFF9FAFB)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
fun MissionPlanningScreen(
    onBack: () -> Unit,
    droneLat: Double = 0.0,
    droneLng: Double = 0.0,
    viewModel: MissionPlanningViewModelContract = koinViewModel<MissionPlanningViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ─── トップバー ───────────────────────────────────────
            MissionTopBar(
                missionStatus = uiState.missionStatus,
                onBack = onBack
            )

            // ─── 地図（上半分） ──────────────────────────────────
            MissionMapView(
                waypoints = uiState.waypoints,
                droneLat = droneLat,
                droneLng = droneLng,
                onMapClick = { lat, lng -> viewModel.addWaypoint(lat, lng) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
            )

            // ─── ミッション進捗バー ──────────────────────────────
            uiState.progress?.let { progress ->
                MissionProgressBar(
                    currentIndex = progress.currentItemIndex,
                    total = progress.missionCount
                )
            }

            // ─── コマンドボタン ──────────────────────────────────
            MissionCommandButtons(
                missionStatus = uiState.missionStatus,
                commandStatus = uiState.commandStatus,
                hasWaypoints = uiState.waypoints.isNotEmpty(),
                onUpload = { viewModel.uploadMission() },
                onStart = { viewModel.startMission() },
                onStop = { viewModel.stopMission() },
                onPause = { viewModel.pauseMission() }
            )

            // ─── ウェイポイントリスト（下半分） ──────────────────
            WaypointList(
                waypoints = uiState.waypoints,
                onEdit = { index -> editingIndex = index },
                onDelete = { index -> viewModel.removeWaypoint(index) },
                modifier = Modifier.weight(0.55f)
            )
        }

        // ─── エラーダイアログ ────────────────────────────────────
        uiState.errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("エラー", color = TextPrimary) },
                text = { Text(message, color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK", color = AccentBlue)
                    }
                },
                containerColor = CardBg
            )
        }

        // ─── ウェイポイント編集ダイアログ ────────────────────────
        editingIndex?.let { index ->
            val item = uiState.waypoints.getOrNull(index)
            if (item != null) {
                WaypointEditDialog(
                    index = index,
                    item = item,
                    onConfirm = { updated ->
                        viewModel.updateWaypoint(index, updated)
                        editingIndex = null
                    },
                    onDismiss = { editingIndex = null }
                )
            }
        }
    }
}

// ============================================================
// MissionTopBar
// ============================================================

@Composable
private fun MissionTopBar(
    missionStatus: MissionStatus,
    onBack: () -> Unit
) {
    val statusLabel = when (missionStatus) {
        MissionStatus.Idle -> "待機中"
        MissionStatus.Uploading -> "送信中"
        MissionStatus.Running -> "実行中"
        MissionStatus.Paused -> "一時停止"
        MissionStatus.Complete -> "完了"
        MissionStatus.Error -> "エラー"
    }
    val statusColor = when (missionStatus) {
        MissionStatus.Running -> AccentGreen
        MissionStatus.Paused -> AccentOrange
        MissionStatus.Error -> AccentRed
        MissionStatus.Complete -> AccentBlue
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = TextPrimary)
        }
        Text(
            text = "ミッション計画",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = statusColor.copy(alpha = 0.2f)
        ) {
            Text(
                text = statusLabel,
                color = statusColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

// ============================================================
// MissionProgressBar
// ============================================================

@Composable
private fun MissionProgressBar(currentIndex: Int, total: Int) {
    val progress = if (total > 0) currentIndex.toFloat() / total.toFloat() else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ウェイポイント進捗", color = TextSecondary, fontSize = 12.sp)
            Text("$currentIndex / $total", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = AccentGreen,
            trackColor = Color(0xFF374151)
        )
    }
}

// ============================================================
// MissionCommandButtons
// ============================================================

@Composable
private fun MissionCommandButtons(
    missionStatus: MissionStatus,
    commandStatus: RunStatus<Unit>?,
    hasWaypoints: Boolean,
    onUpload: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPause: () -> Unit
) {
    val isLoading = commandStatus is RunStatus.Loading

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 送信ボタン
        Button(
            onClick = onUpload,
            enabled = hasWaypoints && !isLoading && missionStatus != MissionStatus.Running,
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            modifier = Modifier.weight(1f)
        ) {
            if (isLoading && missionStatus == MissionStatus.Uploading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("送信", fontSize = 13.sp)
            }
        }

        // 開始ボタン
        Button(
            onClick = onStart,
            enabled = !isLoading && missionStatus != MissionStatus.Running,
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("開始", fontSize = 13.sp)
        }

        // 一時停止ボタン
        Button(
            onClick = onPause,
            enabled = !isLoading && missionStatus == MissionStatus.Running,
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("停止", fontSize = 13.sp)
        }

        // 停止ボタン
        Button(
            onClick = onStop,
            enabled = !isLoading && (missionStatus == MissionStatus.Running || missionStatus == MissionStatus.Paused),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("中止", fontSize = 13.sp)
        }
    }
}

// ============================================================
// WaypointList
// ============================================================

@Composable
private fun WaypointList(
    waypoints: List<MissionItem>,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ウェイポイント", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("地図をタップして追加", color = TextSecondary, fontSize = 11.sp)
        }

        if (waypoints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("ウェイポイントなし", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(waypoints) { index, item ->
                    WaypointRow(
                        index = index,
                        item = item,
                        onEdit = { onEdit(index) },
                        onDelete = { onDelete(index) }
                    )
                }
            }
        }
    }
}

// ============================================================
// WaypointRow
// ============================================================

@Composable
private fun WaypointRow(
    index: Int,
    item: MissionItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = CardBg,
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 番号バッジ
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AccentBlue.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        color = AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${"%.5f".format(item.latitudeDeg)}, ${"%.5f".format(item.longitudeDeg)}",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "高度: ${"%.1f".format(item.altitudeMeters)} m  速度: ${"%.1f".format(item.speedMS)} m/s",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = AccentRed, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ============================================================
// WaypointEditDialog
// ============================================================

@Composable
private fun WaypointEditDialog(
    index: Int,
    item: MissionItem,
    onConfirm: (MissionItem) -> Unit,
    onDismiss: () -> Unit
) {
    var altitudeText by remember { mutableStateOf(item.altitudeMeters.toString()) }
    var speedText by remember { mutableStateOf(item.speedMS.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("WP${index + 1} 編集", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${"%.5f".format(item.latitudeDeg)}, ${"%.5f".format(item.longitudeDeg)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = altitudeText,
                    onValueChange = { altitudeText = it },
                    label = { Text("高度 (m)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = TextSecondary
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = speedText,
                    onValueChange = { speedText = it },
                    label = { Text("速度 (m/s)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = TextSecondary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val altitude = altitudeText.toFloatOrNull() ?: item.altitudeMeters
                    val speed = speedText.toFloatOrNull() ?: item.speedMS
                    onConfirm(item.copy(altitudeMeters = altitude, speedMS = speed))
                }
            ) {
                Text("保存", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = TextSecondary)
            }
        },
        containerColor = CardBg
    )
}

// ============================================================
// Preview
// ============================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MissionPlanningScreenPreview() {
    MissionPlanningScreen(
        onBack = {},
        viewModel = MockMissionPlanningViewModel()
    )
}
