package com.android.carrent.fragments.main


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.carrent.R
import com.android.carrent.activities.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import com.android.carrent.utils.extensions.*


class LoginFragment : Fragment(), View.OnClickListener {
    private var TAG: String = "LoginFragment"

    // Variable to disable action while logging in
    private var disabledWhileLogin = false

    // Firebase
    private var mAuth: FirebaseAuth? = null

    // Fragments
    private lateinit var registerFragment: RegisterFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Init needed fragments
        registerFragment = RegisterFragment()


        // If user is logged in, change fragment to SplashFragment
        mAuth?.currentUser?.let {
            Log.d(TAG, "User is already logged in, starting HomeActivity")
            startHomeActivity()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var v: View = inflater.inflate(R.layout.fragment_login, container, false)

        setLogoAndFormFadeIn(context!!, v.iv_logo, v.login_form)

        v.btn_login.setOnClickListener(this)

        v.tv_goto_register.setOnClickListener(this)

        v.tv_forgot_password.setOnClickListener(this)

        return v
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> {
                Log.d(TAG, "Clicked on login button")
                if (!disabledWhileLogin) doValidations()
            }

            R.id.tv_goto_register -> {
                Log.d(TAG, "Clicked on register textview")
                if (!disabledWhileLogin) {
                    changeFragment(registerFragment)
                }
            }

            R.id.tv_forgot_password -> {
                Log.d(TAG, "Clicked on forgot password textview")
                if (!disabledWhileLogin) {

                }
            }
        }
    }

    private fun doValidations() {
        if (!Patterns.EMAIL_ADDRESS.matcher(et_email.text).matches()) {
            et_email.error = resources.getString(R.string.enter_valid_email)
            et_email.requestFocus()
        } else if (et_password.text?.length == 0) {
            et_password.error = resources.getString(R.string.hint_password)
            et_password.requestFocus()
        } else {
            // Login user
            disabledWhileLogin = true
            showProgressBar(progress_bar)
            login(et_email.text.toString(), et_password.text.toString())

        }
    }

    private fun login(email: String, password: String) {
        mAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "User logged in, starting SplashFragment")
                    startHomeActivity()

                } else {
                    Log.d(TAG, "Something went wrong when logging in")
                    makeToast(it.exception?.message.toString())
                    disabledWhileLogin = false
                    hideProgressBar(progress_bar)
                }
            }
    }

    private fun startHomeActivity() {
        activity?.finish()
        startActivity(Intent(activity, HomeActivity::class.java))
    }
}
