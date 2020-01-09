package com.android.carrent.utils.constants

import android.Manifest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Constants {
    // Firestore
    const val FIRESTORE_USERS_REFERENCE: String = "users"
    const val FIRESTORE_CARS_REFERENCE: String = "cars"

    // Location, location PERMISSIONS
    val PERMISSIONS =
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    const val DEFAULT_ZOOM = 14f
    const val COUNTRY_ZOOM = 6f
    const val UPDATE_INTERVAL: Long = 1000
    const val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    // Bounds of Lithuania
    val LAT_LNG_BOUNDS_OF_LITHUANIA =
        LatLngBounds(LatLng(54.01667, 21.06861), LatLng(56.31667, 26.41667))

    // Requests
    const val LOCATION_PERMISSIONS_REQUEST = 1001
    const val ERROR_DIALOG_REQUEST = 1003
    const val GPS_REQUEST = 1004

    const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    // BackStacks
    const val BACKSTACK_HOME_FRAGMENT = "BACKSTACK_HOME_FRAGMENT"
    const val BACKSTACK_PROFILE_FRAGMENT = "BACKSTACK_PROFILE_FRAGMENT"
    const val BACKSTACK_CAR_FRAGMENT = "BACKSTACK_CAR_FRAGMENT"
    const val BACKSTACK_EDIT_PROFILE_FRAGMENT = "BACKSTACK_EDIT_PROFILE_FRAGMENT"
    const val BACKSTACK_LOGIN_FRAGMENT = "BACKSTACK_LOGIN_FRAGMENT"
    const val BACKSTACK_REGISTER_FRAGMENT = "BACKSTACK_REGISTER_FRAGMENT"
    const val BACKSTACK_CAR_MANAGEMENT_FRAGMENT = "BACKSTACK_CAR_MANAGEMENT_FRAGMENT"

    // Bundle keys
    const val BUNDLE_KEY_CAR_ID = "CAR_ID"

    // Date and time formats
    var DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    const val HOUR_TO_MILLIS = 3600000
    val USER_DECISION_TIME = TimeUnit.SECONDS.toMillis(40)
    val MINIMUM_RENT_TIME = 1 * HOUR_TO_MILLIS - USER_DECISION_TIME

}