package com.io.dronecontroller.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val DEFAULT_LAT = 35.6762
private const val DEFAULT_LNG = 139.6503
private const val MAP_ZOOM = 18.0

@Composable
actual fun DroneMapView(
    latitude: Double,
    longitude: Double,
    bearing: Float,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val hasPosition = latitude != 0.0 || longitude != 0.0
    val initialLat = if (hasPosition) latitude else DEFAULT_LAT
    val initialLng = if (hasPosition) longitude else DEFAULT_LNG

    Configuration.getInstance().userAgentValue = context.packageName

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(MAP_ZOOM)
            controller.setCenter(GeoPoint(initialLat, initialLng))
        }
    }

    val marker = remember {
        Marker(mapView).apply {
            title = "機体位置"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }

    LaunchedEffect(latitude, longitude, bearing) {
        val point = GeoPoint(
            if (hasPosition) latitude else DEFAULT_LAT,
            if (hasPosition) longitude else DEFAULT_LNG,
        )
        marker.position = point
        marker.rotation = bearing
        if (!mapView.overlays.contains(marker)) {
            mapView.overlays.add(marker)
        }
        if (hasPosition) {
            mapView.controller.animateTo(point)
        }
        mapView.invalidate()
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
