package com.android.carrent.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.fragments.home.HomeFragment.HomeFragment
import com.android.carrent.fragments.home.ProfileFragment
import com.android.carrent.fragments.home.RentedFragment
import com.android.carrent.utils.MapServiceGpsRequests
import com.android.carrent.utils.extensions.makeToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private var TAG: String = "HomeActivity"
    private var backPressedTime: Long = 0

    // Firebase
    private var mAuth: FirebaseAuth? = null
    // MapServiceGpsRequests
    private lateinit var mMapServiceGpsRequests: MapServiceGpsRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashThemeHome)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Init firebase
        mAuth = FirebaseAuth.getInstance()
        // Init MapServiceGpsRequests
        mMapServiceGpsRequests = MapServiceGpsRequests(this)
        // Init toolbar
        setSupportActionBar(toolbar as Toolbar)

        if (mAuth?.currentUser == null) {
            Log.d(TAG, "User is not logged in, starting MainActivity")
            startMainActivity()
        }

        if (savedInstanceState == null && mMapServiceGpsRequests.isServicesOk()) {
            setFragment(fragment = HomeFragment())
        }

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                Log.d(TAG, "Clicked on BottomNavigation home_main_menu item")
                if (mMapServiceGpsRequests.isServicesOk()) {
                    setFragment(fragment = HomeFragment())
                    return@OnNavigationItemSelectedListener true
                }

            }
            R.id.navigation_profile -> {
                Log.d(TAG, "Clicked on BottomNavigation profile item")
                supportActionBar?.title = "My Profile"
                setFragment(fragment = ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_rented -> {
                Log.d(TAG, "Clicked on BottomNavigation rented item")
                supportActionBar?.title = "My Car"
                setFragment(fragment = RentedFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_logout -> {
                Log.d(TAG, "Clicked on BottomNavigation logout item")
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    mAuth?.signOut()
                    startMainActivity()
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

    override fun onBackPressed() {
        if (bottom_navigation.selectedItemId == R.id.navigation_home) {
            super.onBackPressed()
        } else bottom_navigation.selectedItemId = R.id.navigation_home

    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun startMainActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}
