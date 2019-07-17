package com.android.carrent.utils

import android.Manifest
import com.android.carrent.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object Constants {
    // FadeIn durations
    val LOGO_ANIMATION_DURATION: Long = 800
    val FORM_ANIMATION_DURATION: Long = 1700

    // Location, location PERMISSIONS
    val PERMISSIONS = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    val DEFAULT_ZOOM = 14f
    val COUNTRY_ZOOM = 6f
    val UPDATE_INTERVAL: Long = 1000
    val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    // Bounds of Lithuania
    val LAT_LNG_BOUNDS_OF_LITHUANIA = LatLngBounds(LatLng(54.01667, 21.06861), LatLng(56.31667, 26.41667))

    // Requests
    val LOCATION_PERMISSIONS_REQUEST = 1001
    val LOCATION_PERMISSIONS_REQUEST_FOR_GPS = 1002
    val ERROR_DIALOG_REQUEST = 1003
    val GPS_REQUEST = 1004

    // SplashFragment delay
    val SPLASH_DELAY: Long = 3000

    // Loading tips
    val listOfLoadingTips = listOf(
        R.string.tip1,
        R.string.tip2,
        R.string.tip3

    )

    val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

}