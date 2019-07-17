package com.android.carrent.fragments.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.carrent.R
import com.android.carrent.activities.HomeActivity
import com.android.carrent.utils.Constants.listOfLoadingTips
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_splash.view.*
import java.util.*
import com.android.carrent.utils.*


class SplashFragment : Fragment() {
    private var TAG: String = "SplashFragment"
    // Splash delay handler
    private lateinit var mDelayHandler: Handler
    // Firebase
    private var mAuth: FirebaseAuth? = null
    // Fragments
    private lateinit var loginFragment: LoginFragment


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

        mDelayHandler.postDelayed({
            startHomeActivity()
        }, Constants.SPLASH_DELAY)

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

    private fun getCustomTip(): Int {
        return listOfLoadingTips[Random().nextInt(listOfLoadingTips.size)]
    }

    private fun startHomeActivity() {
        activity?.finish()
        startActivity(Intent(activity, HomeActivity::class.java))
    }
}
