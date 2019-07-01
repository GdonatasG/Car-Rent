package com.android.carrent.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.fragments.home.HomeFragment
import com.android.carrent.fragments.home.ProfileFragment
import com.android.carrent.fragments.home.RentedFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private var TAG: String = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            setFragment(HomeFragment())
        }

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                Log.d(TAG, "Clicked on BottomNavigation home item")
                setFragment(fragment = HomeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                Log.d(TAG, "Clicked on BottomNavigation profile item")
                setFragment(fragment = ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_rented -> {
                Log.d(TAG, "Clicked on BottomNavigation rented item")
                setFragment(fragment = RentedFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_logout -> {
                Log.d(TAG, "Clicked on BottomNavigation logout item")
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
}
