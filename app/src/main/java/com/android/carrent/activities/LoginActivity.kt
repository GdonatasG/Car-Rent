package com.android.carrent.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.android.carrent.R
import kotlinx.android.synthetic.main.activity_login.*
import com.android.carrent.utils.setLogoAndFormFadeIn


class LoginActivity : AppCompatActivity() {
    var disabledWhileLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setLogoAndFormFadeIn(applicationContext, iv_logo, login_form)

        btn_login.setOnClickListener { if (!disabledWhileLogin) doValidations() }

        tv_goto_register.setOnClickListener {
            if (!disabledWhileLogin) {
                finish()
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }

        tv_forgot_password.setOnClickListener {
            // Forgot password functionality
            if (!disabledWhileLogin) {

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
            progress_bar.visibility = View.VISIBLE
        }
    }
}
