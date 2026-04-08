package com.io.dronecontroller.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.io.dronecontroller.domain.model.MissionItem

// iOS向けミッション地図は将来対応
@Composable
actual fun MissionMapView(
    waypoints: List<MissionItem>,
    droneLat: Double,
    droneLng: Double,
    onMapClick: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B2228)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "地図はAndroid版のみ対応", color = Color.White)
    }
}
