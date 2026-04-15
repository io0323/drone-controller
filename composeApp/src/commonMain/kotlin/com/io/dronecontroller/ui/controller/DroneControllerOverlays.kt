package com.io.dronecontroller.ui.controller

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.RunStatus

// ============================================================
// デザイントークン（カスタマイズポイント）
// ============================================================

/** チップ背景: 黒80%透明 */
private val ChipBg = Color(0xCC000000)

/** 緑系アクセント（通信・バッテリー） */
private val GreenAccent = Color(0xFF4ADE80)

/** 青系アクセント（衛星数） */
private val BlueAccent = Color(0xFF60A5FA)

/** オレンジアクセント（速度） */
private val OrangeAccent = Color(0xFFFB923C)

/** 赤系アクセント（高度） */
private val RedAccent = Color(0xFFFC6868)

// ============================================================
// StatusChips — 左上ステータス群
// ============================================================

/**
 * 画面左上に表示する縦並びのステータスチップ群
 * （通信品質 / バッテリー / 衛星数）
 */
@Composable
fun StatusChips(
    modifier: Modifier = Modifier,
    batteryPercent: Int = 0,
    satelliteCount: Int = 0,
    isConnected: Boolean = false,
    isReconnecting: Boolean = false,
    bleConnectionStatus: BleConnectionStatus = BleConnectionStatus.Disconnected,
    onBack: (() -> Unit)? = null,
) {
    val batteryTint =
        when {
            batteryPercent >= 50 -> GreenAccent
            batteryPercent >= 20 -> OrangeAccent
            else -> RedAccent
        }
    val isBleConnected = bleConnectionStatus is BleConnectionStatus.Connected
    val bleLabel =
        when (bleConnectionStatus) {
            is BleConnectionStatus.Connected -> bleConnectionStatus.device.name.take(8)
            is BleConnectionStatus.Connecting -> "接続中..."
            is BleConnectionStatus.Scanning -> "スキャン中"
            else -> "BLE"
        }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (onBack != null) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ChipBg),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "戻る",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        StatusChip(
            icon = Icons.Default.SignalCellularAlt,
            label =
                when {
                    isReconnecting -> "再接続中"
                    isConnected -> "HD"
                    else -> "--"
                },
            iconTint =
                when {
                    isReconnecting -> OrangeAccent
                    isConnected -> GreenAccent
                    else -> Color.Gray
                },
        )
        StatusChip(
            icon = Icons.Default.Battery6Bar,
            label = "$batteryPercent%",
            iconTint = batteryTint,
        )
        StatusChip(
            icon = Icons.Default.Satellite,
            label = "$satelliteCount",
            iconTint = BlueAccent,
        )
        StatusChip(
            icon = if (isBleConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
            label = if (isBleConnected) bleLabel else "--",
            iconTint = if (isBleConnected) GreenAccent else Color.Gray,
        )
    }
}

/** カプセル型の単一ステータスチップ */
@Composable
private fun StatusChip(
    icon: ImageVector,
    label: String,
    iconTint: Color,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = ChipBg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = iconTint,
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ============================================================
// FlightInfoPanel — 上中央フライト情報
// ============================================================

/**
 * 画面上部中央に表示するフライト情報チップ群
 * （速度 / 高度）
 */
@Composable
fun FlightInfoPanel(
    modifier: Modifier = Modifier,
    altitudeMeters: Float = 0f,
    speedKmh: Float = 0f,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        FlightInfoChip(
            icon = Icons.Default.Speed,
            value = "${"%.1f".format(speedKmh)} km/h",
            iconTint = OrangeAccent,
        )
        FlightInfoChip(
            icon = Icons.Default.LocationOn,
            value = "${"%.1f".format(altitudeMeters)} m",
            iconTint = RedAccent,
        )
    }
}

/** アイコン付きフライト情報の単一チップ */
@Composable
private fun FlightInfoChip(
    icon: ImageVector,
    value: String,
    iconTint: Color,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = ChipBg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = iconTint,
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// ============================================================
// TopRightActionButtons — 右上アクションボタン
// ============================================================

/**
 * 画面右上に表示する縦並びの円形アクションボタン
 * （設定 / ホーム / 地図切替）
 */
@Composable
fun TopRightActionButtons(
    modifier: Modifier = Modifier,
    isMapMode: Boolean = false,
    isBleConnected: Boolean = false,
    isConnected: Boolean = false,
    onToggleMap: () -> Unit = {},
    onOpenBleSettings: () -> Unit = {},
    onOpenMission: () -> Unit = {},
    onOpenDroneConnection: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActionCircleButton(
            icon = if (isBleConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothSearching,
            contentDescription = "BLE設定",
            tint = if (isBleConnected) GreenAccent else Color.White,
            onClick = onOpenBleSettings,
        )
        ActionCircleButton(
            icon = Icons.Default.WifiTethering,
            contentDescription = "ドローン接続",
            tint = if (isConnected) GreenAccent else Color.White,
            onClick = onOpenDroneConnection,
        )
        ActionCircleButton(
            icon = if (isMapMode) Icons.Default.Navigation else Icons.Default.Map,
            contentDescription = if (isMapMode) "カメラ映像に切替" else "地図に切替",
            tint = if (isMapMode) GreenAccent else Color.White,
            onClick = onToggleMap,
        )
        ActionCircleButton(
            icon = Icons.Default.Route,
            contentDescription = "ミッション計画",
            tint = BlueAccent,
            onClick = onOpenMission,
        )
    }
}

/** 黒半透明の円形アクションボタン */
@Composable
private fun ActionCircleButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1.0f, animationSpec = tween(100))
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .scale(scale)
                .size(38.dp)
                .clip(CircleShape)
                .background(ChipBg),
    ) {
        IconButton(onClick = onClick, enabled = enabled, interactionSource = interactionSource) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
                tint = if (enabled) tint else tint.copy(alpha = 0.3f),
            )
        }
    }
}

// ============================================================
// CenterTargetMarker — 中央ターゲットマーカー
// ============================================================

/**
 * 画面中央に表示するリング状のターゲットマーカー
 * ドローン視点の照準点として機能する
 */
@Composable
fun CenterTargetMarker(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.size(22.dp),
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // 外リング
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx()),
        )
        // 中心の点
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = 2.dp.toPx(),
            center = center,
        )
    }
}

// ============================================================
// CameraActionButtons — 下中央カメラ操作
// ============================================================

@Composable
fun CameraActionButtons(
    modifier: Modifier = Modifier,
    leftX: Float = 0f,
    leftY: Float = 0f,
    rightX: Float = 0f,
    rightY: Float = 0f,
    isConnected: Boolean = false,
    isCapturingPhoto: Boolean = false,
    isRecording: Boolean = false,
    onCapturePhoto: () -> Unit = {},
    onToggleRecording: () -> Unit = {},
) {
    val photoInteractionSource = remember { MutableInteractionSource() }
    val isPhotoPressed by photoInteractionSource.collectIsPressedAsState()
    val photoScale by animateFloatAsState(targetValue = if (isPhotoPressed) 0.85f else 1.0f, animationSpec = tween(100))
    val videoInteractionSource = remember { MutableInteractionSource() }
    val isVideoPressed by videoInteractionSource.collectIsPressedAsState()
    val videoScale by animateFloatAsState(targetValue = if (isVideoPressed) 0.85f else 1.0f, animationSpec = tween(100))
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 写真撮影ボタン（大）
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .scale(photoScale)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (isConnected && !isCapturingPhoto) Color.White else Color(0xFF888888))
                    .then(
                        if (isConnected && !isCapturingPhoto) {
                            Modifier
                        } else {
                            Modifier
                        },
                    ),
        ) {
            IconButton(
                onClick = onCapturePhoto,
//                enabled = isConnected && !isCapturingPhoto,
                // Mock
                enabled = true,
                interactionSource = photoInteractionSource,
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "撮影",
                    modifier = Modifier.size(26.dp),
                    tint = if (isConnected && !isCapturingPhoto) Color(0xFF1A1A2E) else Color.White.copy(alpha = 0.4f),
                )
            }
        }

        // ジョイスティック座標バッジ
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xDD000000),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "左: X: ${"%.2f".format(leftX)}, Y: ${"%.2f".format(leftY)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "右: X: ${"%.2f".format(rightX)}, Y: ${"%.2f".format(rightY)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // 動画ボタン（小）
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .scale(videoScale)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) {
                            Color(0xFFDC2626)
                        } else if (isConnected) {
                            Color.White
                        } else {
                            Color(0xFF888888)
                        }
                    ),
//                    .background(Color(0xFFDC2626)), //Mock
        ) {
            IconButton(
                onClick = onToggleRecording,
//                enabled = isConnected,
                // Mock
                enabled = true,
                interactionSource = videoInteractionSource,
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "動画",
                    modifier = Modifier.size(22.dp),
                    tint = if (isRecording || !isConnected) Color.White.copy(alpha = if (isConnected) 1f else 0.4f) else Color(0xFF1A1A2E),
                )
            }
        }
    }
}

// ============================================================
// HelpButton — 右下端ヘルプボタン
// ============================================================

/**
 * 画面右下端の小さな「?」ヘルプボタン
 */
@Composable
fun HelpButton(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ChipBg),
    ) {
        Text(
            text = "?",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ============================================================
// CommandButtons — 中央コマンドボタン群（離陸・着陸・RTL）
// ============================================================

/**
 * 画面中央下部に表示するフライトコマンドボタン群
 *
 * @param isConnected    接続中フラグ（falseなら全ボタン非活性）
 * @param isArmed        アーム済みフラグ（falseなら離陸ボタン非活性）
 * @param commandStatus  コマンド実行状態（ローディング・エラー表示に使用）
 * @param onTakeoff      離陸コールバック
 * @param onLand         着陸コールバック
 * @param onReturnToLaunch RTLコールバック
 */
@Composable
fun CommandButtons(
    modifier: Modifier = Modifier,
    isConnected: Boolean = false,
    isArmed: Boolean = false,
    commandStatus: RunStatus<Unit>? = null,
    onTakeoff: () -> Unit = {},
    onLand: () -> Unit = {},
    onReturnToLaunch: () -> Unit = {},
) {
    val isLoading = commandStatus is RunStatus.Loading
    val errorMessage = (commandStatus as? RunStatus.Error)?.message

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        }
        if (errorMessage != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xCCCC3333),
            ) {
                Text(
                    text = errorMessage,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CommandButton(
                icon = Icons.Default.KeyboardArrowUp,
                contentDescription = "離陸",
//                enabled = isConnected && isArmed && !isLoading,
                enabled = true, // Mock
                onClick = onTakeoff,
            )
            CommandButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "着陸",
//                enabled = isConnected && !isLoading,
                enabled = true, // Mock
                onClick = onLand,
            )
            CommandButton(
                icon = Icons.Default.Undo,
                contentDescription = "RTL",
//                enabled = isConnected && !isLoading,
                enabled = true, // Mock
                onClick = onReturnToLaunch,
            )
        }
    }
}

@Composable
private fun CommandButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1.0f, animationSpec = tween(100))
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .scale(scale)
                .size(44.dp)
                .clip(CircleShape)
                .background(ChipBg),
    ) {
        IconButton(onClick = onClick, enabled = enabled, interactionSource = interactionSource) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                tint = if (enabled) Color.White else Color.White.copy(alpha = 0.3f),
            )
        }
    }
}

// ============================================================
// DroneConnectionPanel — ドローン接続管理パネル
// ============================================================

@Composable
fun DroneConnectionPanel(
    isConnected: Boolean = false,
    onDismiss: () -> Unit = {},
    onDisconnect: () -> Unit = {},
    onReconnect: (ip: String, port: Int) -> Unit = { _, _ -> },
) {
    var ip by remember { mutableStateOf("10.0.2.2") }
    var portText by remember { mutableStateOf("50051") }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .then(Modifier.padding(0.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss),
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF0121212),
            modifier = Modifier.padding(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = null,
                        tint = if (isConnected) GreenAccent else Color.Gray,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "ドローン接続",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                HorizontalDivider(color = Color(0xFF2A2A2A))
                if (isConnected) {
                    Text(
                        text = "接続中: $ip:$portText",
                        color = GreenAccent,
                        fontSize = 13.sp,
                    )
                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("切断", color = Color.White)
                    }
                } else {
                    Text(
                        text = "未接続",
                        color = Color.Gray,
                        fontSize = 13.sp,
                    )
                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("IPアドレス", color = Color.Gray, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it },
                        label = { Text("ポート", color = Color.Gray, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onReconnect(ip, portText.toIntOrNull() ?: 50051) },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("再接続", color = Color.White)
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("閉じる", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

// ============================================================
// Preview
// ============================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun CommandButtonsConnectedPreview() {
    CommandButtons(isConnected = true, isArmed = true)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun CommandButtonsDisconnectedPreview() {
    CommandButtons(isConnected = false, isArmed = false)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun StatusChipsPreview() {
    StatusChips(batteryPercent = 87, satelliteCount = 12, isConnected = true)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun FlightInfoPanelPreview() {
    FlightInfoPanel(altitudeMeters = 25.3f, speedKmh = 12.5f)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun CameraActionButtonsPreview() {
    CameraActionButtons(leftX = 0.35f, leftY = -0.12f, rightX = 0.0f, rightY = 0.0f, isConnected = true)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun CameraActionButtonsRecordingPreview() {
    CameraActionButtons(leftX = 0f, leftY = 0f, rightX = 0f, rightY = 0f, isConnected = true, isRecording = true)
}
