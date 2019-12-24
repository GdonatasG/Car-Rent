package com.android.carrent.activities

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.fragments.HomeFragment.HomeFragment
import com.android.carrent.fragments.authentication.LoginFragment
import com.android.carrent.utils.ConnectivityReceiver
import com.android.carrent.utils.MapServiceGpsRequests
import com.android.carrent.utils.extensions.mainSnackbarView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private var TAG: String = "MainActivity"

    // Internet connection receiver
    var isInternetOn: Boolean = false
    private var snackbar: Snackbar? = null
    private var connectivityReceiver: ConnectivityReceiver = ConnectivityReceiver()

    // Firebase
    private var mAuth: FirebaseAuth? = null
    // MapServiceGpsRequests
    private lateinit var mMapServiceGpsRequests: MapServiceGpsRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.android.carrent.R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(com.android.carrent.R.layout.activity_main)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // Init internet connection receiver
        registerReceiver(
            connectivityReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        // Init firebase
        mAuth = FirebaseAuth.getInstance()

        // Init MapServiceGpsRequests
        mMapServiceGpsRequests = MapServiceGpsRequests(this)

        // Init toolbar
        setSupportActionBar(toolbar as Toolbar)

        //bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (savedInstanceState == null && mMapServiceGpsRequests.isServicesOk()) {
            setFragment(fragment = HomeFragment())
        } else {
            setFragment(fragment = LoginFragment())
        }

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        isInternetOn = isConnected
        println("CONNECTION CHANGED TO: ${isInternetOn}")
        showNetworkMessage()
    }

    fun showNetworkMessage() {
        if (!isInternetOn) {
            snackbar = Snackbar.make(
                connectivitySnack,
                getText(com.android.carrent.R.string.error_no_internet),
                Snackbar.LENGTH_LONG
            ).setAction(getString(R.string.snackbar_action_close)) {
                snackbar?.dismiss()
            }
            snackbar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            mainSnackbarView(snackbar = snackbar, activity = this)
            snackbar?.show()
        } else {
            snackbar?.dismiss()
        }
    }

    fun enabledWidgets() {
        toolbar.visibility = View.VISIBLE
    }

    fun disableWidgets() {
        toolbar.visibility = View.GONE
    }

    fun setToolbarTitle(title: String?) {
        supportActionBar?.title = title
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(com.android.carrent.R.id.container_host, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onDestroy() {
        unregisterReceiver(connectivityReceiver)
        super.onDestroy()
    }
}
