package com.io.dronecontroller.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.io.dronecontroller.domain.model.MissionItem

/**
 * ミッション計画用地図コンポーザブル
 *
 * @param waypoints     ウェイポイントリスト
 * @param droneLat      機体緯度
 * @param droneLng      機体経度
 * @param onMapClick    地図タップ時のコールバック（緯度・経度）
 * @param modifier      Modifier
 */
@Composable
expect fun MissionMapView(
    waypoints: List<MissionItem>,
    droneLat: Double,
    droneLng: Double,
    onMapClick: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier,
)
