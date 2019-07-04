package com.android.carrent.fragments.main

import android.Manifest.permission.*
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi

import com.android.carrent.R
import com.android.carrent.activities.HomeActivity
import com.android.carrent.utils.Constants.SPLASH_DELAY
import com.android.carrent.utils.Constants.listOfLoadingTips
import com.android.carrent.utils.changeFragment
import com.android.carrent.utils.hideProgressBar
import com.android.carrent.utils.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.android.synthetic.main.fragment_splash.view.*
import java.util.*
import androidx.appcompat.app.AlertDialog
import com.android.carrent.utils.Constants.PERMISSIONS_REQUEST_ENABLE_GPS
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.android.carrent.utils.Constants.ERROR_DIALOG_REQUEST


class SplashFragment : Fragment() {
    private var TAG: String = "SplashFragment"

    // Splash delay handler
    private lateinit var mDelayHandler: Handler

    // Firebase
    private var mAuth: FirebaseAuth? = null

    // Fragments
    private lateinit var loginFragment: LoginFragment

    // RxPermissions
    private lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Init needed fragments
        loginFragment = LoginFragment()

        // If user is not logged in, change fragment to LoginFragment
        if (mAuth?.currentUser == null) {
            Log.d(TAG, "User is not logged in, starting LoginFragment")
            changeFragment(loginFragment)
        }

        // Delay handler
        mDelayHandler = Handler()

        // Requesting permissions
        rxPermissions = RxPermissions(activity!!)
        rxPermissions.setLogging(true)
        if (checkMapServices()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Requesting permissions")
                requestPermissions()
            }


        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_splash, container, false)


        // Setting custom quick tip
        v.tv_tip.text = resources.getString(R.string.loading_tip_prefix) + " " +
                resources.getString(getCustomTip())

        return v
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestPermissions() {
        rxPermissions
            .request(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_NETWORK_STATE)
            .subscribe { granted ->
                if (granted) {
                    Log.d(TAG, "All permissions granted, starting HomeActivity")
                    mDelayHandler.postDelayed({
                        hideProgressBar(progress_bar)
                        startHomeActivity()

                    }, SPLASH_DELAY)
                } else {
                    rxPermissions.shouldShowRequestPermissionRationale(
                        activity,
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        ACCESS_NETWORK_STATE
                    )
                        .subscribe { canAskAgain ->
                            if (canAskAgain) {
                                Log.d(TAG, "User denied permissions without ask never again")
                                signOut()
                                makeToast(resources.getString(R.string.permissions_error))
                            } else {
                                Log.d(TAG, "User denied permissions with ask never again")
                                signOut()
                                makeToast(resources.getString(R.string.permissions_error_never))
                            }

                        }
                }
            }
    }

    private fun signOut() {
        Log.d(TAG, "Signing out..")
        mAuth?.signOut()
        changeFragment(loginFragment)
    }

    private fun getCustomTip(): Int {
        return listOfLoadingTips[Random().nextInt(listOfLoadingTips.size)]
    }

    private fun startHomeActivity() {
        activity?.finish()
        startActivity(Intent(activity, HomeActivity::class.java))
    }

    private fun checkMapServices(): Boolean {
        if (isServicesOk()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    private fun isMapsEnabled(): Boolean {
        var locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "Gps disabled")
            buildAlertMessageNoGps()
            return false
        }

        return true
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(activity!!)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                val enableGpsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
            }
        val alert = builder.create()
        alert.show()
    }

    private fun isServicesOk(): Boolean {
        var available: Int = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google API connection is ok")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            val dialog: Dialog =
                GoogleApiAvailability.getInstance().getErrorDialog(activity, available, ERROR_DIALOG_REQUEST)
            dialog.show()
        } else makeToast(resources.getString(R.string.maps_error))

        return false
    }
}
