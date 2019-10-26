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
    private var backPressedTime: Long = 0
    private var snackbar: Snackbar? = null

    // Firebase
    private var mAuth: FirebaseAuth? = null
    // MapServiceGpsRequests
    private lateinit var mMapServiceGpsRequests: MapServiceGpsRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // Init internet connection receiver
        registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

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
        showNetworkMessage(isConnected)
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            snackbar = Snackbar.make(
                connectivitySnack,
                getText(R.string.error_no_internet),
                Snackbar.LENGTH_LONG
            )
            snackbar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            mainSnackbarView(snackbar = snackbar)
            if (!snackbar!!.isShown) snackbar?.show()
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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
}
