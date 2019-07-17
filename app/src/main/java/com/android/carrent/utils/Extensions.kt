package com.android.carrent.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.utils.Constants.COUNTRY_ZOOM
import com.android.carrent.utils.Constants.DEFAULT_ZOOM
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds


fun setLogoAndFormFadeIn(context: Context, iv_logo: ImageView, form: LinearLayout) {
    val logoAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    logoAnimation.duration = Constants.LOGO_ANIMATION_DURATION

    val formAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    formAnimation.duration = Constants.FORM_ANIMATION_DURATION

    iv_logo.startAnimation(logoAnimation)
    form.startAnimation(formAnimation)
}

fun Fragment.makeToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, duration).show()
}

fun Activity.makeToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(applicationContext, message, duration).show()
}

fun showProgressBar(b: ProgressBar) {
    b.visibility = View.VISIBLE
}

fun hideProgressBar(b: ProgressBar) {
    b.visibility = View.INVISIBLE
}

fun Fragment.changeFragment(fragment: Fragment) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.replace(R.id.container, fragment)
        ?.commitAllowingStateLoss()
}

fun setCameraView(googleMap: GoogleMap, location: Location?) {
    googleMap.moveCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(location!!.latitude, location.longitude),
            DEFAULT_ZOOM
        )
    )
}

fun setCameraViewWBounds(googleMap: GoogleMap, bounds: LatLngBounds) {
    googleMap.moveCamera(
        CameraUpdateFactory.newLatLngZoom(
            bounds.center,
            COUNTRY_ZOOM
        )
    )
}

@SuppressLint("MissingPermission")
fun enableDeviceLocationWButton(googleMap: GoogleMap){
    googleMap.isMyLocationEnabled = true
    googleMap.uiSettings.isMyLocationButtonEnabled = true
}
