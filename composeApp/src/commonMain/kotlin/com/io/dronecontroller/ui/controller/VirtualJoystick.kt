package com.io.dronecontroller.ui.controller

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 仮想ジョイスティック
 *
 * @param label ジョイスティック上に表示するラベル
 * @param joystickSize 外径サイズ（dp）
 * @param onValueChanged ドラッグ中の正規化済み座標コールバック (-1.0 〜 1.0)
 */
@Composable
fun VirtualJoystick(
    label: String,
    modifier: Modifier = Modifier,
    joystickSize: Dp = 130.dp,
    onValueChanged: (x: Float, y: Float) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()

    // ノブ位置をAnimatableで管理（ドラッグ中はsnapTo、リリース時はspring animateToで中央復帰）
    val knobX = remember { Animatable(0f) }
    val knobY = remember { Animatable(0f) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ラベル
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(joystickSize)
                .pointerInput(Unit) {
                    // PointerInputScope.size はピクセル単位 — dp変換不要
                    val maxRadius = size.width / 2f * 0.70f

                    detectDragGestures(
                        onDragEnd = {
                            // 指を離したらバネアニメで中央に戻す
                            scope.launch {
                                launch {
                                    knobX.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                                launch {
                                    knobY.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                            onValueChanged(0f, 0f)
                        },
                        onDragCancel = {
                            scope.launch {
                                knobX.snapTo(0f)
                                knobY.snapTo(0f)
                            }
                            onValueChanged(0f, 0f)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newX = knobX.value + dragAmount.x
                            val newY = knobY.value + dragAmount.y
                            val distance = sqrt(newX * newX + newY * newY)

                            // 移動範囲を円内に制限
                            val (clampedX, clampedY) = if (distance <= maxRadius) {
                                newX to newY
                            } else {
                                val scale = maxRadius / distance
                                newX * scale to newY * scale
                            }

                            scope.launch { knobX.snapTo(clampedX) }
                            scope.launch { knobY.snapTo(clampedY) }

                            onValueChanged(
                                (clampedX / maxRadius).coerceIn(-1f, 1f),
                                (clampedY / maxRadius).coerceIn(-1f, 1f)
                            )
                        }
                    )
                }
        ) {
            // ジョイスティックのベース（外円・内リング・クロスライン）をCanvasで描画
            Canvas(modifier = Modifier.size(joystickSize)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = size.minDimension / 2f
                val innerRadius = outerRadius * 0.42f

                // 外円の薄い塗り
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = outerRadius,
                    center = center
                )
                // 外円のリング
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
                // 内側のリング
                drawCircle(
                    color = Color.White.copy(alpha = 0.18f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                // 縦クロスライン
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(center.x, center.y - outerRadius),
                    end = Offset(center.x, center.y + outerRadius),
                    strokeWidth = 0.7.dp.toPx()
                )
                // 横クロスライン
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(center.x - outerRadius, center.y),
                    end = Offset(center.x + outerRadius, center.y),
                    strokeWidth = 0.7.dp.toPx()
                )
            }

            // ノブ — Animatableの値でオフセット
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            knobX.value.roundToInt(),
                            knobY.value.roundToInt()
                        )
                    }
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.38f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.75f), CircleShape)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF1A2024)
@Composable
private fun VirtualJoystickPreview() {
    VirtualJoystick(label = "上昇/回転")
}
