package com.android.carrent.fragments


import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.carrent.R
import com.android.carrent.models.User
import com.android.carrent.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*

class RegisterFragment : Fragment(), View.OnClickListener {
    private var disabledWhileRegister = false
    private val TAG: String = "RegisterFragment"
    private var mAuth: FirebaseAuth? = null
    private lateinit var splashFragment: SplashFragment
    private lateinit var loginFragment: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        splashFragment = SplashFragment()
        loginFragment = LoginFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_register, container, false)
        mAuth?.currentUser?.let {
            Log.d(TAG, "User is already logged in, starting SplashFragment")
            changeFragment(splashFragment)
        }

        Log.d(TAG, "User is not logged in")
        setLogoAndFormFadeIn(context!!, v.iv_logo, v.register_form)

        v.btn_register.setOnClickListener(this)

        v.tv_goto_login.setOnClickListener(this)
        return v
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.btn_register -> {
                if (!disabledWhileRegister) doValidations()
            }
            R.id.tv_goto_login -> {
                if (!disabledWhileRegister) {
                    changeFragment(loginFragment)
                }
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
            Log.d(TAG, "Validations are done")
            disabledWhileRegister = true
            showProgressBar(progress_bar)
            register(
                et_username.text.toString(),
                et_email.text.toString(),
                et_password.text.toString(),
                et_phone.text.toString(),
                0.0,
                0
            )
        }
    }

    private fun register(
        username: String,
        email: String,
        password: String,
        phone: String,
        balance: Double?,
        rentedCarId: Int?
    ) {
        mAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Registration completed!")
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

                    val user = User(username, email, phone, balance, rentedCarId)
                    ref.setValue(user).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "User added into database!")
                            changeFragment(splashFragment)
                        } else {
                            makeToast(it.exception?.message.toString())
                            hideProgressBar(progress_bar)
                            disabledWhileRegister = false
                        }
                    }
                } else {
                    makeToast(it.exception?.message.toString())
                    hideProgressBar(progress_bar)
                    disabledWhileRegister = false
                }
            }

    }
}
