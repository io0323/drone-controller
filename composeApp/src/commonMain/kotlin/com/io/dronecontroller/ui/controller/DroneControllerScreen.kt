package com.io.dronecontroller.ui.controller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.ui.ble.BleSettingsScreen
import com.io.dronecontroller.ui.map.DroneMapView
import org.koin.compose.viewmodel.koinViewModel

/**
 * ドローンコントローラーのメイン操作画面
 *
 * 構成:
 *  - CameraBackground   ドローンカメラ映像風の背景
 *  - ScreenGridOverlay  3x3分割グリッド線
 *  - StatusChips        左上ステータス群
 *  - FlightInfoPanel    上中央フライト情報
 *  - TopRightActionButtons 右上アクションボタン
 *  - CenterTargetMarker 中央照準マーカー
 *  - CameraActionButtons 下中央カメラ操作（ジョイスティック値を表示）
 *  - VirtualJoystick x2 左下・右下の仮想スティック
 *  - HelpButton         右下端ヘルプボタン
 */
@Composable
fun DroneControllerScreen(
    onNavigateToMission: (lat: Double, lng: Double) -> Unit = { _, _ -> },
    viewModel: DroneControllerViewModelContract = koinViewModel<DroneControllerViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startObserving()
    }

    var showBleSettings by remember { mutableStateOf(false) }

    // 左右ジョイスティックの値を管理（-1.0〜1.0）
    var leftJoystickX by remember { mutableStateOf(0f) }
    var leftJoystickY by remember { mutableStateOf(0f) }
    var rightJoystickX by remember { mutableStateOf(0f) }
    var rightJoystickY by remember { mutableStateOf(0f) }

    // BLE接続中は物理コントローラー優先のため仮想ジョイスティック入力を抑制
    val isBleConnected = uiState.bleConnectionStatus is BleConnectionStatus.Connected

    Box(modifier = Modifier.fillMaxSize()) {

        // ─── 背景（地図 or カメラ映像風） ──────────────────────────
        if (uiState.isMapMode) {
            DroneMapView(
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                bearing = uiState.bearing,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CameraBackground()
            ScreenGridOverlay()
        }

        // ─── 左上ステータス ───────────────────────────────────────
        StatusChips(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 14.dp),
            batteryPercent = uiState.batteryPercent,
            satelliteCount = uiState.satelliteCount,
            isConnected = uiState.connectionStatus is ConnectionStatus.Connected,
            bleConnectionStatus = uiState.bleConnectionStatus
        )

        // ─── 上中央フライト情報 ──────────────────────────────────
        FlightInfoPanel(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp),
            altitudeMeters = uiState.altitudeMeters,
            speedKmh = uiState.speedKmh
        )

        // ─── 右上アクションボタン ────────────────────────────────
        TopRightActionButtons(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 14.dp),
            isMapMode = uiState.isMapMode,
            isBleConnected = isBleConnected,
            onToggleMap = { viewModel.toggleMapMode() },
            onOpenBleSettings = { showBleSettings = true },
            onOpenMission = { onNavigateToMission(uiState.latitude, uiState.longitude) }
        )

        // ─── 中央ターゲットマーカー ──────────────────────────────
        CenterTargetMarker(
            modifier = Modifier.align(Alignment.Center)
        )

        // ─── 中央: コマンドボタン（離陸・着陸・RTL） ────────────
        CommandButtons(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 80.dp),
            isConnected = uiState.connectionStatus is ConnectionStatus.Connected,
            isArmed = uiState.isArmed,
            commandStatus = uiState.commandStatus,
            onTakeoff = { viewModel.takeoff() },
            onLand = { viewModel.land() },
            onReturnToLaunch = { viewModel.returnToLaunch() }
        )

        // ─── 下中央カメラボタン（ジョイスティック値を反映） ───────
        CameraActionButtons(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            leftX = leftJoystickX,
            leftY = leftJoystickY,
            rightX = rightJoystickX,
            rightY = rightJoystickY
        )

        // ─── 左下: 上昇/回転スティック（BLE接続時はグレーアウト） ──
        VirtualJoystick(
            label = if (isBleConnected) "上昇/回転 (BLE)" else "上昇/回転",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 22.dp),
            onValueChanged = { x, y ->
                leftJoystickX = x
                leftJoystickY = y
                if (!isBleConnected) {
                    viewModel.updateJoystickInput(x, y, rightJoystickX, rightJoystickY)
                }
            }
        )

        // ─── 右下: 移動スティック（BLE接続時はグレーアウト） ─────
        VirtualJoystick(
            label = if (isBleConnected) "移動 (BLE)" else "移動",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // HelpButtonの分（34dp）だけ内側に寄せる
                .padding(end = 44.dp, bottom = 22.dp),
            onValueChanged = { x, y ->
                rightJoystickX = x
                rightJoystickY = y
                if (!isBleConnected) {
                    viewModel.updateJoystickInput(leftJoystickX, leftJoystickY, x, y)
                }
            }
        )

        // ─── 右下端: ヘルプボタン ────────────────────────────────
        HelpButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 10.dp)
        )

        // ─── BLE設定オーバーレイ ─────────────────────────────────
        if (showBleSettings) {
            BleSettingsScreen(onDismiss = { showBleSettings = false })
        }
    }
}

// ============================================================
// CameraBackground — 背景
// ============================================================

/**
 * ドローンのカメラ映像を模した背景
 * 実映像がない場合は航空写真風のCanvasプレースホルダーを表示する
 */
@Composable
fun CameraBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // ─── ベース背景 ─────────────────────────────────────────
        drawRect(color = Color(0xFF1B2228))

        // ─── 道路（水平）─────────────────────────────────────────
        drawRect(
            color = Color(0xFF28333A),
            topLeft = Offset(0f, size.height * 0.44f),
            size = Size(size.width, size.height * 0.13f)
        )
        // 道路センターライン（破線風）
        val dashCount = 12
        repeat(dashCount) { i ->
            val x = size.width / dashCount * i + size.width / dashCount * 0.2f
            val w = size.width / dashCount * 0.6f
            drawRect(
                color = Color(0xFF4A5560),
                topLeft = Offset(x, size.height * 0.498f),
                size = Size(w, size.height * 0.006f)
            )
        }

        // ─── 道路（垂直）─────────────────────────────────────────
        drawRect(
            color = Color(0xFF28333A),
            topLeft = Offset(size.width * 0.37f, 0f),
            size = Size(size.width * 0.13f, size.height)
        )
        val dashCountV = 18
        repeat(dashCountV) { i ->
            val y = size.height / dashCountV * i + size.height / dashCountV * 0.2f
            val h = size.height / dashCountV * 0.6f
            drawRect(
                color = Color(0xFF4A5560),
                topLeft = Offset(size.width * 0.432f, y),
                size = Size(size.width * 0.006f, h)
            )
        }

        // ─── 建物ブロック（4隅）──────────────────────────────────
        val blockColor = Color(0xFF222C33)
        // 左上
        drawRect(blockColor, Offset(0f, 0f), Size(size.width * 0.35f, size.height * 0.42f))
        // 右上
        drawRect(blockColor, Offset(size.width * 0.52f, 0f), Size(size.width * 0.48f, size.height * 0.42f))
        // 左下
        drawRect(blockColor, Offset(0f, size.height * 0.59f), Size(size.width * 0.35f, size.height * 0.41f))
        // 右下
        drawRect(blockColor, Offset(size.width * 0.52f, size.height * 0.59f), Size(size.width * 0.48f, size.height * 0.41f))

        // ─── 建物の細部ディテール ─────────────────────────────────
        val detailColor = Color(0xFF2E3B44)
        // 左上ブロック内の建物
        drawRect(detailColor, Offset(size.width * 0.03f, size.height * 0.05f), Size(size.width * 0.14f, size.height * 0.18f))
        drawRect(detailColor, Offset(size.width * 0.20f, size.height * 0.08f), Size(size.width * 0.10f, size.height * 0.12f))
        // 右上ブロック内の建物
        drawRect(detailColor, Offset(size.width * 0.55f, size.height * 0.04f), Size(size.width * 0.18f, size.height * 0.20f))
        drawRect(detailColor, Offset(size.width * 0.77f, size.height * 0.10f), Size(size.width * 0.15f, size.height * 0.14f))

        // ─── 全体に薄い暗幕を重ねてUIを見やすくする ──────────────
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x55000000), Color(0x33000000), Color(0x55000000))
            )
        )
    }
}

// ============================================================
// ScreenGridOverlay — 3x3グリッド線
// ============================================================

/**
 * カメラ映像上に重ねる主張しすぎない3x3グリッド線
 */
@Composable
fun ScreenGridOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = Color.White.copy(alpha = 0.13f)
        val sw = 0.6.dp.toPx()

        // 縦線 (1/3, 2/3)
        drawLine(lineColor, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), sw)
        drawLine(lineColor, Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), sw)

        // 横線 (1/3, 2/3)
        drawLine(lineColor, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), sw)
        drawLine(lineColor, Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), sw)
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
private fun DroneControllerScreenPreview() {
    DroneControllerScreen(viewModel = MockDroneControllerViewModel())
}
