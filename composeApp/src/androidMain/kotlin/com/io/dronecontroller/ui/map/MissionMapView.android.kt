package com.io.dronecontroller.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.io.dronecontroller.domain.model.MissionItem
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private const val DEFAULT_LAT = 35.6762
private const val DEFAULT_LNG = 139.6503
private const val MAP_ZOOM = 17.0

@Composable
actual fun MissionMapView(
    waypoints: List<MissionItem>,
    droneLat: Double,
    droneLng: Double,
    onMapClick: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val hasPosition = droneLat != 0.0 || droneLng != 0.0
    val centerLat =
        when {
            waypoints.isNotEmpty() -> waypoints.first().latitudeDeg
            hasPosition -> droneLat
            else -> DEFAULT_LAT
        }
    val centerLng =
        when {
            waypoints.isNotEmpty() -> waypoints.first().longitudeDeg
            hasPosition -> droneLng
            else -> DEFAULT_LNG
        }

    Configuration.getInstance().userAgentValue = context.packageName

    val mapView =
        remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(MAP_ZOOM)
                controller.setCenter(GeoPoint(centerLat, centerLng))
            }
        }

    LaunchedEffect(waypoints, droneLat, droneLng) {
        mapView.overlays.clear()

        if (waypoints.size >= 2) {
            val polyline =
                Polyline(mapView).apply {
                    outlinePaint.color =
                        androidx.compose.ui.graphics
                            .Color(0xFF00BFFF)
                            .toArgb()
                    outlinePaint.strokeWidth = 5f
                    setPoints(waypoints.map { GeoPoint(it.latitudeDeg, it.longitudeDeg) })
                }
            mapView.overlays.add(polyline)
        }

        waypoints.forEachIndexed { index, item ->
            Marker(mapView).apply {
                position = GeoPoint(item.latitudeDeg, item.longitudeDeg)
                title = "WP${index + 1}"
                snippet = "高度: ${"%.1f".format(item.altitudeMeters)} m  速度: ${"%.1f".format(item.speedMS)} m/s"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(this)
            }
        }

        if (hasPosition) {
            Marker(mapView).apply {
                position = GeoPoint(droneLat, droneLng)
                title = "機体位置"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mapView.overlays.add(this)
            }
        }

        mapView.invalidate()
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.setOnTouchListener { v, event ->
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    val projection = view.projection
                    val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                    onMapClick(geoPoint.latitude, geoPoint.longitude)
                }
                v.onTouchEvent(event)
            }
        },
    )
}
