package com.android.carrent.utils.extensions

import android.annotation.SuppressLint
import android.location.Location
import com.android.carrent.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

fun setCameraView(googleMap: GoogleMap, location: Location?) {
    googleMap.moveCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(location!!.latitude, location.longitude),
            Constants.DEFAULT_ZOOM
        )
    )
}

fun setCameraViewWBounds(googleMap: GoogleMap, bounds: LatLngBounds) {
    googleMap.moveCamera(
        CameraUpdateFactory.newLatLngZoom(
            bounds.center,
            Constants.COUNTRY_ZOOM
        )
    )
}

@SuppressLint("MissingPermission")
fun enableDeviceLocationWButton(googleMap: GoogleMap) {
    googleMap.isMyLocationEnabled = true
    googleMap.uiSettings.isMyLocationButtonEnabled = true
}