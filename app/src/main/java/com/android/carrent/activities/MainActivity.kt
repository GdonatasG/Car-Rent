package com.android.carrent.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.fragments.logged.HomeFragment.HomeFragment
import com.android.carrent.fragments.logged.ProfileFragment
import com.android.carrent.fragments.logged.RentedFragment
import com.android.carrent.fragments.unlogged.LoginFragment
import com.android.carrent.utils.MapServiceGpsRequests
import com.android.carrent.utils.extensions.makeToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var TAG: String = "MainActivity"
    private var backPressedTime: Long = 0

    // Firebase
    private var mAuth: FirebaseAuth? = null
    // MapServiceGpsRequests
    private lateinit var mMapServiceGpsRequests: MapServiceGpsRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // Init firebase
        mAuth = FirebaseAuth.getInstance()

        // Init MapServiceGpsRequests
        mMapServiceGpsRequests = MapServiceGpsRequests(this)

        // Init toolbar
        setSupportActionBar(toolbar as Toolbar)

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (mAuth?.currentUser == null) {
            Log.d(TAG, "User is not logged in, starting LoginFragment")
            setFragment(fragment = LoginFragment())
        } else {
            if (savedInstanceState == null && mMapServiceGpsRequests.isServicesOk()) {
                setFragment(fragment = HomeFragment())
                enabledWidgets()
            }
        }


    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                Log.d(TAG, "Clicked on BottomNavigation home_main_menu item")
                if (mMapServiceGpsRequests.isServicesOk()) {
                    setFragment(fragment = HomeFragment())
                    enabledWidgets()
                    return@OnNavigationItemSelectedListener true
                }

            }
            R.id.navigation_profile -> {
                Log.d(TAG, "Clicked on BottomNavigation profile item")
                supportActionBar?.title = "My Profile"
                setFragment(fragment = ProfileFragment())
                enabledWidgets()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_rented -> {
                Log.d(TAG, "Clicked on BottomNavigation rented item")
                supportActionBar?.title = "My Car"
                setFragment(fragment = RentedFragment())
                enabledWidgets()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_logout -> {
                Log.d(TAG, "Clicked on BottomNavigation logout item")
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    mAuth?.signOut()
                    setFragment(fragment = LoginFragment())
                } else {
                    makeToast(resources.getString(R.string.logout_press_again))
                }

                backPressedTime = System.currentTimeMillis()

                // Return false, because no reason to select this item, it`s only logout button
                return@OnNavigationItemSelectedListener false
            }

        }
        false
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
}
