package com.io.dronecontroller.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private const val DEFAULT_LAT = 35.6762 // 東京（フォールバック）
private const val DEFAULT_LNG = 139.6503
private const val MAP_ZOOM = 18f

@Composable
actual fun DroneMapView(
    latitude: Double,
    longitude: Double,
    bearing: Float,
    modifier: Modifier,
) {
    val hasPosition = latitude != 0.0 || longitude != 0.0
    val initialLat = if (hasPosition) latitude else DEFAULT_LAT
    val initialLng = if (hasPosition) longitude else DEFAULT_LNG

    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), MAP_ZOOM)
        }

    LaunchedEffect(latitude, longitude) {
        if (hasPosition) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)),
            )
        }
    }

    val markerState =
        remember(latitude, longitude) {
            MarkerState(position = LatLng(initialLat, initialLng))
        }

    LaunchedEffect(latitude, longitude) {
        if (hasPosition) {
            markerState.position = LatLng(latitude, longitude)
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.SATELLITE),
    ) {
        Marker(
            state = markerState,
            rotation = bearing,
            flat = true,
            anchor =
                androidx.compose.ui.geometry
                    .Offset(0.5f, 0.5f),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
            title = "機体位置",
            snippet = "Alt: ${"%.1f".format(if (hasPosition) 0f else 0f)} m",
        )
    }
}
