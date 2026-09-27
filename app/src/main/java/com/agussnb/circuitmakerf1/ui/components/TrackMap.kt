package com.agussnb.circuitmakerf1.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private val DEFAULT_POSITION = LatLng(-34.6037, -58.3816)
private const val DEFAULT_ZOOM = 17f

@Composable
fun TrackMap(
    points: List<GpsPoint>,
    modifier: Modifier = Modifier
) {
    val latLngs = remember(points) {
        points.map { LatLng(it.latitude, it.longitude) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_POSITION, DEFAULT_ZOOM)
    }

    val lastPoint = latLngs.lastOrNull()
    LaunchedEffect(lastPoint) {
        if (lastPoint != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(lastPoint))
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        if (latLngs.size >= 2) {
            Polyline(
                points = latLngs,
                color = Color.Red,
                width = 12f
            )
        }
    }
}