package com.android.carrent.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.android.carrent.R
import com.android.carrent.utils.setLogoAndFormFadeIn
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    var disabledWhileRegister = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setLogoAndFormFadeIn(applicationContext, iv_logo, register_form)

        btn_register.setOnClickListener { if (!disabledWhileRegister) doValidations() }

        tv_goto_login.setOnClickListener {
            if (!disabledWhileRegister) {
                finish()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    private fun doValidations() {
        if (et_username.text.length == 0) {
            et_username.error = getText(R.string.hint_username)
            et_username.requestFocus()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(et_email.text).matches()) {
            et_email.error = getText(R.string.enter_valid_email)
            et_email.requestFocus()
        } else if (et_password.text.length == 0) {
            et_password.error = getText(R.string.hint_password)
            et_password.requestFocus()
        } else if (et_password_confirm.text.length == 0) {
            et_password_confirm.error = getText(R.string.hint_confirm_password)
            et_password_confirm.requestFocus()
        } else if (!et_password.text.toString().equals(et_password_confirm.text.toString())) {
            et_password.error = getText(R.string.passwords_no_match)
            et_password.requestFocus()
        } else if (et_phone.text.length == 0) {
            et_phone.error = getText(R.string.hint_phone_number)
            et_phone.requestFocus()
        } else {
            // Register user
            disabledWhileRegister = true
            progress_bar.visibility = View.VISIBLE
        }
    }
}
