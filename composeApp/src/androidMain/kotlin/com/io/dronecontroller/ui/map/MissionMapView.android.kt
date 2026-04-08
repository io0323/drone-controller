package com.io.dronecontroller.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.io.dronecontroller.domain.model.MissionItem

private const val DEFAULT_LAT = 35.6762
private const val DEFAULT_LNG = 139.6503
private const val MAP_ZOOM = 17f

@Composable
actual fun MissionMapView(
    waypoints: List<MissionItem>,
    droneLat: Double,
    droneLng: Double,
    onMapClick: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier
) {
    val hasPosition = droneLat != 0.0 || droneLng != 0.0
    val centerLat = when {
        waypoints.isNotEmpty() -> waypoints.first().latitudeDeg
        hasPosition -> droneLat
        else -> DEFAULT_LAT
    }
    val centerLng = when {
        waypoints.isNotEmpty() -> waypoints.first().longitudeDeg
        hasPosition -> droneLng
        else -> DEFAULT_LNG
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLng), MAP_ZOOM)
    }

    val waypointPositions = remember(waypoints) {
        waypoints.map { LatLng(it.latitudeDeg, it.longitudeDeg) }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.SATELLITE),
        onMapClick = { latLng -> onMapClick(latLng.latitude, latLng.longitude) }
    ) {
        // ウェイポイント間を結ぶ線
        if (waypointPositions.size >= 2) {
            Polyline(
                points = waypointPositions,
                color = androidx.compose.ui.graphics.Color(0xFF00BFFF),
                width = 5f
            )
        }

        // ウェイポイントマーカー（番号付き）
        waypoints.forEachIndexed { index, item ->
            Marker(
                state = MarkerState(position = LatLng(item.latitudeDeg, item.longitudeDeg)),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                title = "WP${index + 1}",
                snippet = "高度: ${"%.1f".format(item.altitudeMeters)} m  速度: ${"%.1f".format(item.speedMS)} m/s"
            )
        }

        // 機体位置マーカー
        if (hasPosition) {
            Marker(
                state = MarkerState(position = LatLng(droneLat, droneLng)),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                title = "機体位置"
            )
        }
    }
}
