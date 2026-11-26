package com.agussnb.circuitmakerf1.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import com.agussnb.circuitmakerf1.domain.model.TrackLayout
import com.agussnb.circuitmakerf1.domain.service.endpoints
import com.agussnb.circuitmakerf1.ui.theme.F1Red
import com.agussnb.circuitmakerf1.ui.theme.SectorColors
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private val DEFAULT_POSITION = LatLng(-34.6037, -58.3816)
private const val DEFAULT_ZOOM = 17f
private const val BOUNDS_PADDING_PX = 120

private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)

/**
 * Mapa del circuito.
 *
 * - Sin [layout]: dibuja el recorrido crudo ([path]) en rojo, como mientras grabás.
 * - Con [layout]: dibuja los 3 sectores con sus colores y la línea de meta.
 * - [currentPosition]: punto blanco con tu ubicación actual.
 * - [followPosition]: si es `true`, la cámara te sigue; si no, encuadra el circuito entero.
 *
 * La conversión de nuestro `GeoPoint` al `LatLng` de Google se hace acá, en la UI:
 * el dominio no conoce Google Maps.
 */
@Composable
fun TrackMap(
    modifier: Modifier = Modifier,
    path: List<GeoPoint> = emptyList(),
    layout: TrackLayout? = null,
    currentPosition: GeoPoint? = null,
    followPosition: Boolean = false
) {
    val rawLine = remember(path) { path.map { it.toLatLng() } }
    val sectorLines = remember(layout) {
        layout?.sectors?.map { sector -> sector.map { it.toLatLng() } } ?: emptyList()
    }
    val finishLine = remember(layout) {
        layout?.gates?.firstOrNull()?.endpoints()?.let { (a, b) -> listOf(a.toLatLng(), b.toLatLng()) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            path.firstOrNull()?.toLatLng() ?: currentPosition?.toLatLng() ?: DEFAULT_POSITION,
            DEFAULT_ZOOM
        )
    }
    var mapLoaded by remember { mutableStateOf(false) }

    // Seguir la posición actual (al grabar).
    val follow = if (followPosition) currentPosition?.toLatLng() else null
    LaunchedEffect(follow) {
        if (follow != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(follow))
        }
    }

    // Encuadrar el circuito completo (en detalle y en la sesión). Solo se puede
    // calcular el encuadre una vez que el mapa terminó de cargar y tiene tamaño.
    LaunchedEffect(mapLoaded, layout, followPosition) {
        val points = layout?.sectors?.flatten()
        if (mapLoaded && !followPosition && !points.isNullOrEmpty()) {
            val bounds = LatLngBounds.builder().apply {
                points.forEach { include(it.toLatLng()) }
            }.build()
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING_PX))
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
        onMapLoaded = { mapLoaded = true }
    ) {
        if (sectorLines.isNotEmpty()) {
            sectorLines.forEachIndexed { index, line ->
                Polyline(points = line, color = SectorColors[index % SectorColors.size], width = 14f)
            }
            finishLine?.let { Polyline(points = it, color = Color.White, width = 10f, zIndex = 1f) }
        } else if (rawLine.size >= 2) {
            Polyline(points = rawLine, color = F1Red, width = 12f)
        }

        currentPosition?.let {
            Circle(
                center = it.toLatLng(),
                radius = 6.0,
                fillColor = Color.White,
                strokeColor = Color.Black,
                strokeWidth = 4f,
                zIndex = 2f
            )
        }
    }
}
