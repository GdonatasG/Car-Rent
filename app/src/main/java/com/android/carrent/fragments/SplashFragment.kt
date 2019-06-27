package com.android.carrent.fragments


import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.carrent.R
import com.android.carrent.utils.Constants.SPLASH_DELAY
import com.android.carrent.utils.changeFragment
import com.android.carrent.utils.hideProgressBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.android.synthetic.main.fragment_splash.view.*

class SplashFragment : Fragment() {
    private lateinit var mDelayHandler: Handler
    private var mAuth: FirebaseAuth? = null
    private lateinit var loginFragment: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        loginFragment = LoginFragment()

        mDelayHandler = Handler()

        mDelayHandler.postDelayed({
            hideProgressBar(progress_bar)
        }, SPLASH_DELAY)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_splash, container, false)

        v.iv_logo.setOnClickListener {
            mAuth?.signOut()
            changeFragment(loginFragment)
        }
        return v
    }

}
