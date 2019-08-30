package com.android.carrent.activities

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.fragments.HomeFragment.HomeFragment
import com.android.carrent.fragments.ProfileFragment
import com.android.carrent.fragments.RentedFragment
import com.android.carrent.fragments.authentication.LoginFragment
import com.android.carrent.utils.ConnectivityReceiver
import com.android.carrent.utils.MapServiceGpsRequests
import com.android.carrent.utils.extensions.mainSnackbarView
import com.android.carrent.utils.extensions.makeToast
import com.google.android.material.bottomnavigation.BottomNavigationView
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

        // Init internet connectio receiver
        registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        // Init firebase
        mAuth = FirebaseAuth.getInstance()

        // Init MapServiceGpsRequests
        mMapServiceGpsRequests = MapServiceGpsRequests(this)

        // Init toolbar
        setSupportActionBar(toolbar as Toolbar)

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (savedInstanceState == null && mMapServiceGpsRequests.isServicesOk()) {
            setFragment(fragment = HomeFragment())
        } else {
            setFragment(fragment = LoginFragment())
        }

    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                Log.d(TAG, "Clicked on BottomNavigation home_main_menu item")
                if (mMapServiceGpsRequests.isServicesOk()) {
                    setFragment(fragment = HomeFragment())
                    return@OnNavigationItemSelectedListener true
                } else {
                    setFragment(fragment = LoginFragment())
                }

            }
            R.id.navigation_profile -> {
                Log.d(TAG, "Clicked on BottomNavigation profile item")
                if (mAuth?.currentUser != null) {
                    supportActionBar?.title = "My Profile"
                    setFragment(fragment = ProfileFragment())
                    return@OnNavigationItemSelectedListener true
                } else setFragment(fragment = LoginFragment())

            }
            R.id.navigation_rented -> {
                Log.d(TAG, "Clicked on BottomNavigation rented item")
                if (mAuth?.currentUser != null) {
                    supportActionBar?.title = "My Car"
                    setFragment(fragment = RentedFragment())
                    return@OnNavigationItemSelectedListener true
                } else setFragment(fragment = LoginFragment())
            }
            R.id.navigation_logout -> {
                Log.d(TAG, "Clicked on BottomNavigation logout item")
                if (mAuth?.currentUser != null) {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        mAuth?.signOut()
                        setFragment(fragment = LoginFragment())
                    } else {
                        makeToast(resources.getString(R.string.logout_press_again))
                    }

                    backPressedTime = System.currentTimeMillis()
                }


                // Return false, because no reason to select this item, it`s only logout button
                return@OnNavigationItemSelectedListener false
            }

        }
        false
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
        bottom_navigation.visibility = View.VISIBLE
        toolbar.visibility = View.VISIBLE
    }

    fun disableWidgets() {
        bottom_navigation.visibility = View.GONE
        toolbar.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (bottom_navigation.selectedItemId == R.id.navigation_home) {
            super.onBackPressed()
        } else bottom_navigation.selectedItemId = R.id.navigation_home

    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_host, fragment)
            .commitAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }
}
