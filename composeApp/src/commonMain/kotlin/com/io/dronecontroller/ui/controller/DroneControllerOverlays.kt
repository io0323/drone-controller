package com.io.dronecontroller.ui.controller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
fun StatusChips(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        StatusChip(
            icon = Icons.Default.SignalCellularAlt,
            label = "HD",
            iconTint = GreenAccent
        )
        StatusChip(
            icon = Icons.Default.Battery6Bar,
            label = "87%",
            iconTint = GreenAccent
        )
        StatusChip(
            icon = Icons.Default.Satellite,
            label = "12",
            iconTint = BlueAccent
        )
    }
}

/** カプセル型の単一ステータスチップ */
@Composable
private fun StatusChip(
    icon: ImageVector,
    label: String,
    iconTint: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = ChipBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = iconTint
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================
// FlightInfoPanel — 上中央フライト情報
// ============================================================

/**
 * 画面上部中央に表示するフライト情報チップ群
 * （距離 / 速度 / 高度）
 */
@Composable
fun FlightInfoPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        FlightInfoChip(
            icon = Icons.Default.Navigation,
            value = "45.2m",
            iconTint = BlueAccent
        )
        FlightInfoChip(
            icon = Icons.Default.Speed,
            value = "12.5 km/h",
            iconTint = OrangeAccent
        )
        FlightInfoChip(
            icon = Icons.Default.LocationOn,
            value = "234m",
            iconTint = RedAccent
        )
    }
}

/** アイコン付きフライト情報の単一チップ */
@Composable
private fun FlightInfoChip(
    icon: ImageVector,
    value: String,
    iconTint: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = ChipBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = iconTint
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================================
// TopRightActionButtons — 右上アクションボタン
// ============================================================

/**
 * 画面右上に表示する縦並びの円形アクションボタン
 * （設定 / ホーム）
 */
@Composable
fun TopRightActionButtons(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActionCircleButton(
            icon = Icons.Default.Settings,
            contentDescription = "設定"
        )
        ActionCircleButton(
            icon = Icons.Default.Home,
            contentDescription = "ホーム"
        )
    }
}

/** 黒半透明の円形アクションボタン */
@Composable
private fun ActionCircleButton(
    icon: ImageVector,
    contentDescription: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(ChipBg)
    ) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
                tint = Color.White
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
        modifier = modifier.size(22.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // 外リング
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        // 中心の点
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = 2.dp.toPx(),
            center = center
        )
    }
}

// ============================================================
// CameraActionButtons — 下中央カメラ操作
// ============================================================

/**
 * 画面下中央のカメラ操作ボタン群
 *
 * @param leftX  左ジョイスティックX値（-1.0〜1.0）
 * @param leftY  左ジョイスティックY値（-1.0〜1.0）
 * @param rightX 右ジョイスティックX値（-1.0〜1.0）
 * @param rightY 右ジョイスティックY値（-1.0〜1.0）
 */
@Composable
fun CameraActionButtons(
    modifier: Modifier = Modifier,
    leftX: Float = 0f,
    leftY: Float = 0f,
    rightX: Float = 0f,
    rightY: Float = 0f
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 写真撮影ボタン（大）
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .shadow(elevation = 6.dp, shape = CircleShape)
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "撮影",
                modifier = Modifier.size(26.dp),
                tint = Color(0xFF1A1A2E)
            )
        }

        // ジョイスティック座標バッジ
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xDD000000)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                // 左右の値を2行で表示
                Text(
                    text = "左: X: ${"%.2f".format(leftX)}, Y: ${"%.2f".format(leftY)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "右: X: ${"%.2f".format(rightX)}, Y: ${"%.2f".format(rightY)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 動画ボタン（小）
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .shadow(elevation = 4.dp, shape = CircleShape)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "動画",
                modifier = Modifier.size(22.dp),
                tint = Color(0xFF1A1A2E)
            )
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
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(ChipBg)
    ) {
        Text(
            text = "?",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// Preview
// ============================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun StatusChipsPreview() {
    StatusChips()
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun FlightInfoPanelPreview() {
    FlightInfoPanel()
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun CameraActionButtonsPreview() {
    CameraActionButtons(leftX = 0.35f, leftY = -0.12f, rightX = 0.0f, rightY = 0.0f)
}
