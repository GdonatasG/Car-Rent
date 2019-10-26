package com.android.carrent.fragments.profile

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.android.carrent.R
import com.android.carrent.fragments.HomeFragment.HomeFragment
import com.android.carrent.utils.extensions.changeFragment
import com.android.carrent.utils.extensions.makeToast
import com.google.firebase.auth.FirebaseAuth

class ProfilePreferenceFragment : PreferenceFragmentCompat(), PreferenceManager.OnPreferenceTreeClickListener {
    // Firebase
    private var mAuth: FirebaseAuth? = null
    private var backPressedTime: Long = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.profile_preferences)
        // Init firebase
        mAuth = FirebaseAuth.getInstance()

    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            resources.getString(R.string.KEY_MY_CAR) -> {
                println("my car preference")
            }

            resources.getString(R.string.KEY_EDIT_PROFILE) -> {
                println("edit profile preference")
            }

            resources.getString(R.string.KEY_CHANGE_PASSWORD) -> {
                println("change pass pref")
            }

            resources.getString(R.string.KEY_LOGOUT) -> {
                logout()
            }
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun logout() {
        if (mAuth?.currentUser != null) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                mAuth?.signOut()
                changeFragment(R.id.container_host, HomeFragment())
            } else {
                makeToast(resources.getString(R.string.logout_press_again))
            }

            backPressedTime = System.currentTimeMillis()
        }
    }
}