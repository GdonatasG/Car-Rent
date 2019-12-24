package com.android.carrent.fragments.authentication


import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.fragments.HomeFragment.HomeFragment
import com.android.carrent.models.User.Rent
import com.android.carrent.models.User.User
import com.android.carrent.utils.constants.Constants
import com.android.carrent.utils.extensions.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.android.synthetic.main.fragment_register.view.iv_logo
import kotlinx.android.synthetic.main.fragment_register.view.tv_continue_as_guest

class RegisterFragment : Fragment(), View.OnClickListener {
    private val TAG: String = "RegisterFragment"

    // Variable to disable action while register
    private var disabledWhileRegister = false

    // Firebase
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Disable widgets
        (activity as MainActivity).disableWidgets()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_register, container, false)

        setLogoAndFormFadeIn(context!!, v.iv_logo, v.register_form)

        v.btn_register.setOnClickListener(this)

        v.tv_continue_as_guest.setOnClickListener(this)

        v.tv_goto_login.setOnClickListener(this)

        return v
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.btn_register -> {
                if (!disabledWhileRegister) doValidations()
            }

            R.id.tv_continue_as_guest -> {
                changeFragment(R.id.container_host, fragment = HomeFragment())
            }

            R.id.tv_goto_login -> {
                if (!disabledWhileRegister) {
                    changeFragment(R.id.container_host, fragment = LoginFragment())
                }
            }
        }
    }

    private fun doValidations() {
        if (et_username.text.length == 0) {
            et_username.error = resources.getString(R.string.hint_username)
            et_username.requestFocus()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(et_email.text).matches()) {
            et_email.error = resources.getString(R.string.enter_valid_email)
            et_email.requestFocus()
        } else if (et_password.text.length == 0) {
            et_password.error = resources.getString(R.string.hint_password)
            et_password.requestFocus()
        } else if (et_password_confirm.text.length == 0) {
            et_password_confirm.error = resources.getString(R.string.hint_confirm_password)
            et_password_confirm.requestFocus()
        } else if (!et_password.text.toString().equals(et_password_confirm.text.toString())) {
            et_password.error = resources.getString(R.string.passwords_no_match)
            et_password.requestFocus()
        } else if (et_phone.text.length == 0) {
            et_phone.error = resources.getString(R.string.hint_phone_number)
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
                0f
            )
        }
    }

    private fun register(
        username: String,
        email: String,
        password: String,
        phone: String,
        balance: Float?
    ) {
        mAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Registration completed!")
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    val ref =
                        FirebaseFirestore.getInstance()
                            .collection(Constants.FIRESTORE_USERS_REFERENCE).document(uid)

                    val user = User(
                        uid,
                        username,
                        email,
                        phone,
                        balance,
                        Rent(false, null, null, 0)
                    )
                    ref.set(user)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d(TAG, "User added into firestore!")
                                changeFragment(R.id.container_host, fragment = HomeFragment())
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
