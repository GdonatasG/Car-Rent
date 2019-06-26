package com.android.carrent.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import com.android.carrent.R
import com.android.carrent.utils.hideProgressBar
import com.android.carrent.utils.makeToast
import kotlinx.android.synthetic.main.activity_login.*
import com.android.carrent.utils.setLogoAndFormFadeIn
import com.android.carrent.utils.showProgressBar
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var disabledWhileLogin = false
    private var TAG: String = "LoginActivity"
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        mAuth?.currentUser?.let {
            Log.d(TAG, "User is already logged in, starting SplashActivity")
            finish()
            startSplashActivity()
        }

        setLogoAndFormFadeIn(applicationContext, iv_logo, login_form)

        btn_login.setOnClickListener(this)

        tv_goto_register.setOnClickListener(this)

        tv_forgot_password.setOnClickListener(this)

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
                    startRegisterActivity()
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
            et_email.error = getText(R.string.enter_valid_email)
            et_email.requestFocus()
        } else if (et_password.text.length == 0) {
            et_password.error = getText(R.string.hint_password)
            et_password.requestFocus()
        } else {
            // Login user
            disabledWhileLogin = true
            showProgressBar(progress_bar)
            login(et_email.text.toString(), et_password.text.toString())
        }
    }

    private fun startSplashActivity() {
        finish()
        //startActivity(Intent(this, SplashActivity::class.java))
    }

    private fun startRegisterActivity() {
        finish()
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun login(email: String, password: String) {
        mAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "User logged in, starting SplashActivity")
                    startSplashActivity()

                } else {
                    Log.d(TAG, "Something went wrong when logging in")
                    makeToast(it.exception?.message.toString())
                    disabledWhileLogin = false
                    hideProgressBar(progress_bar)
                }
            }
    }
}
