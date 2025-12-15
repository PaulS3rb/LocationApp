package com.example.locationapp.ui.Pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun MapPage() {

    val sydney = LatLng(-34.0, 151.0)

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            sydney,
            10f
        )
    }

    GoogleMap(
        modifier = Modifier,
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = sydney),
            title = "Marker in Sydney"
        )
    }
}
