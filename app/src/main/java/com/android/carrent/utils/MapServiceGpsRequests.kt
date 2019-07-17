package com.android.carrent.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.startActivityForResult
import com.android.carrent.R
import com.android.carrent.utils.Constants.GPS_REQUEST
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class MapServiceGpsRequests(var activity: Activity) {
    private var TAG: String = "MapServiceGpsRequests"

    fun isServicesOk(): Boolean {
        var available: Int = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google API connection is ok")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            val dialog: Dialog =
                GoogleApiAvailability.getInstance().getErrorDialog(activity, available, Constants.ERROR_DIALOG_REQUEST)
            dialog.show()
        } else activity.makeToast(activity.resources!!.getString(R.string.maps_error))

        return false
    }

    private val isLocationEnabled: Boolean
        get() {
            val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

    fun checkLocation(): Boolean {
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle("Enable Location")
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
            .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivityForResult(myIntent, GPS_REQUEST)
            }
            .setCancelable(false)
        dialog.show()
    }


}