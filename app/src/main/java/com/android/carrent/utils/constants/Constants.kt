package com.android.carrent.utils.constants

import android.Manifest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    // FadeIn durations
    val LOGO_ANIMATION_DURATION: Long = 800
    val FORM_ANIMATION_DURATION: Long = 1700

    // Firestore
    val FIRESTORE_USERS_REFERENCE: String = "users"
    val FIRESTORE_CARS_REFERENCE: String = "cars"

    // Location, location PERMISSIONS
    val PERMISSIONS =
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    val DEFAULT_ZOOM = 14f
    val COUNTRY_ZOOM = 6f
    val UPDATE_INTERVAL: Long = 1000
    val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    // Bounds of Lithuania
    val LAT_LNG_BOUNDS_OF_LITHUANIA =
        LatLngBounds(LatLng(54.01667, 21.06861), LatLng(56.31667, 26.41667))

    // Requests
    val LOCATION_PERMISSIONS_REQUEST = 1001
    val ERROR_DIALOG_REQUEST = 1003
    val GPS_REQUEST = 1004

    val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    // BackStacks
    val BACKSTACK_HOME_FRAGMENT = "BACKSTACK_HOME_FRAGMENT"
    val BACKSTACK_PROFILE_FRAGMENT = "BACKSTACK_PROFILE_FRAGMENT"
    val BACKSTACK_CAR_FRAGMENT = "BACKSTACK_CAR_FRAGMENT"
    val BACKSTACK_EDIT_PROFILE_FRAGMENT = "BACKSTACK_EDIT_PROFILE_FRAGMENT"

    // Bundle keys
    val BUNDLE_KEY_CAR_ID = "CAR_ID"

    // Date and time formats
    var DATE_FORMAT = SimpleDateFormat("dd.MM.YYYY, HH:mm", Locale.getDefault())

}