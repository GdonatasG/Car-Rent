package com.android.carrent.fragments.profile

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.fragments.profile.edit.EditProfileFragment
import com.android.carrent.models.Car.Car
import com.android.carrent.models.User.User
import com.android.carrent.utils.constants.Constants.BACKSTACK_EDIT_PROFILE_FRAGMENT
import com.android.carrent.utils.constants.Constants.DATE_FORMAT
import com.android.carrent.utils.extensions.*
import com.google.firebase.auth.FirebaseAuth

class ProfilePreferenceFragment : PreferenceFragmentCompat(),
    PreferenceManager.OnPreferenceTreeClickListener {

    private var backPressedTime: Long = 0

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null

    // ViewModel
    private lateinit var viewModel: ProfileViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.profile_preferences)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            mAuth = it
        }

        loadUser(mAuth.currentUser?.uid)

        shouldShowHomeButton(activity, true)

    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            resources.getString(R.string.KEY_MY_CAR) -> {
                mAuth.currentUser?.reload()
                if (mAuth.currentUser != null) {
                    // *Ensure that user hasCar
                    // **Add CarManagement fragment, bundle: CAR_ID
                } else {
                    mAuth.signOut()
                }


            }

            resources.getString(R.string.KEY_ADD_FUNDS) -> {
                // add funds functionality
                if ((activity as MainActivity).isInternetOn) {
                    mAuth.currentUser?.reload()
                    if (mAuth.currentUser != null) {

                    } else {
                        mAuth.signOut()
                    }
                } else (activity as MainActivity).showNetworkMessage()
            }

            resources.getString(R.string.KEY_EDIT_PROFILE) -> {
                mAuth.currentUser?.reload()
                if (mAuth.currentUser != null) {
                    changeFragmentWithBackstack(
                        R.id.container_host,
                        EditProfileFragment(),
                        BACKSTACK_EDIT_PROFILE_FRAGMENT
                    )
                } else mAuth.signOut()
            }

            resources.getString(R.string.KEY_LOGOUT) -> {
                logout()
            }
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun loadUser(uid: String?) {
        uid?.let {
            viewModel.getUser(uid).observe(this, Observer<User> { u ->
                if (u != null) updatePreferences(u)
            })
        }
    }

    private fun updatePreferences(user: User) {
        updateUserCarPreference(user)
    }

    private fun updateUserCarPreference(user: User) {
        val pref = findPreference<Preference>(resources.getString(R.string.KEY_MY_CAR))
        if (user.rent?.hasCar!!) {
            viewModel.getCarById(user.rent?.rentedCarId!!).observe(this, Observer<Car> {
                pref?.title = it.model.title
                pref?.summary =
                    resources.getString(R.string.rented_until) + " " + DATE_FORMAT.format(it.rent.rentedUntil?.toDate())
            })
        } else {
            pref?.title = resources.getString(R.string.my_car_title)
            pref?.summary = resources.getString(R.string.my_car_summary)
        }

    }

    private fun logout() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            mAuth.signOut()
        } else {
            makeToast(resources.getString(R.string.logout_press_again))
        }

        backPressedTime = System.currentTimeMillis()
    }

    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener!!)
        super.onStart()
    }

    override fun onStop() {
        if (mAuthStateListener != null) FirebaseAuth.getInstance().removeAuthStateListener(
            mAuthStateListener!!
        )
        super.onStop()
    }
}