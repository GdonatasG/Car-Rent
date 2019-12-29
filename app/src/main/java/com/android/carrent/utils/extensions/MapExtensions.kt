package com.android.carrent.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import com.android.carrent.R
import com.android.carrent.utils.constants.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.io.IOException
import java.util.*

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
fun enableDeviceLocationWButton(googleMap: GoogleMap?) {
    googleMap?.isMyLocationEnabled = true
    googleMap?.uiSettings?.isMyLocationButtonEnabled = true
}

fun getDistance(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double {
    val latA = Math.toRadians(lat1!!)
    val lonA = Math.toRadians(lon1!!)


    val latB = Math.toRadians(lat2!!)
    val lonB = Math.toRadians(lon2!!)
    val cosAng = Math.cos(latA) * Math.cos(latB) * Math.cos(lonB - lonA) + Math.sin(latA) * Math.sin(latB)
    val ang = Math.acos(cosAng)
    return ang * 6371
}

fun getAddress(lat: Double, lng: Double, context: Context): Address {
    var addresses: List<Address> = listOf(Address(Locale.getDefault()))

    try {
        addresses = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
    } catch (e: IOException) {
        Toast.makeText(context, context.getText(R.string.address_error), Toast.LENGTH_SHORT).show()
    }

    return addresses[0]


}