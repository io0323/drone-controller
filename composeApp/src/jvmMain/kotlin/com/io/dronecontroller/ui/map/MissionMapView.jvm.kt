package com.io.dronecontroller.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.io.dronecontroller.domain.model.MissionItem

// JVM（Desktop）向けミッション地図は将来対応
@Composable
actual fun MissionMapView(
    waypoints: List<MissionItem>,
    droneLat: Double,
    droneLng: Double,
    onMapClick: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier,
) {
    Box(modifier = modifier.background(Color(0xFF1B2228)))
}
