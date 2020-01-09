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
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private var TAG: String = "MainActivity"

    private var snackbar: Snackbar? = null
    var mSnackbar: Snackbar? = null

    // Internet connection receiver
    var isInternetOn: Boolean = false
    private var connectivityReceiver: ConnectivityReceiver = ConnectivityReceiver()

    // MapServiceGpsRequests
    private lateinit var mMapServiceGpsRequests: MapServiceGpsRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.android.carrent.R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(com.android.carrent.R.layout.activity_main)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        initSnackbar()

        // Init internet connection receiver
        registerReceiver(
            connectivityReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        // Init MapServiceGpsRequests
        mMapServiceGpsRequests = MapServiceGpsRequests(this)

        // Init toolbar
        setSupportActionBar(toolbar as Toolbar)

        if (savedInstanceState == null && mMapServiceGpsRequests.isServicesOk()) {
            setFragment(fragment = HomeFragment())
        } else {
            setFragment(fragment = LoginFragment())
        }

    }

    private fun initSnackbar() {
        mSnackbar = Snackbar.make(
            connectivitySnack,
            getText(R.string.ERROR_NO_CAR),
            Snackbar.LENGTH_LONG
        ).setAction(resources.getText(R.string.snackbar_action_close)) {
            mSnackbar?.dismiss()
        }
        mSnackbar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
        mainSnackbarView(snackbar = mSnackbar, context = this)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        isInternetOn = isConnected
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
            mainSnackbarView(snackbar = snackbar, context = this)
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
        popBackStack()
        return true
    }

    override fun onBackPressed() {
        popBackStack()
    }

    private fun popBackStack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else super.onBackPressed()
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_host, fragment)
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
