package com.io.dronecontroller.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// JVM（Desktop）向け地図は将来対応
@Composable
actual fun DroneMapView(
    latitude: Double,
    longitude: Double,
    bearing: Float,
    modifier: Modifier,
) {
    Box(modifier = modifier.background(Color(0xFF1B2228)))
}
