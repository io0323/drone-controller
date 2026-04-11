package com.io.dronecontroller.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 機体位置を表示する地図コンポーザブル
 *
 * @param latitude  緯度（度）
 * @param longitude 経度（度）
 * @param bearing   機首方位（度、0=北）
 * @param modifier  Modifier
 */
@Composable
expect fun DroneMapView(
    latitude: Double,
    longitude: Double,
    bearing: Float,
    modifier: Modifier = Modifier,
)
