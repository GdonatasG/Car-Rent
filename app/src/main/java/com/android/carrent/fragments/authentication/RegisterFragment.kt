package com.android.carrent.fragments.authentication


import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.models.User.Rent
import com.android.carrent.models.User.User
import com.android.carrent.utils.constants.Constants
import com.android.carrent.utils.constants.Constants.BACKSTACK_LOGIN_FRAGMENT
import com.android.carrent.utils.extensions.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.android.synthetic.main.fragment_register.view.tv_continue_as_guest

class RegisterFragment : Fragment(), View.OnClickListener {
    private val TAG: String = "RegisterFragment"

    private lateinit var mAuth: FirebaseAuth

    // Context (to ensure that not null)
    private lateinit var mContext: Context

    // ProgressDialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        // Disable widgets
        (activity as MainActivity).disableWidgets()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_register, container, false)

        v.btn_register.setOnClickListener(this)

        v.tv_continue_as_guest.setOnClickListener(this)

        v.tv_goto_login.setOnClickListener(this)

        // Init progress dialog
        progressDialog = ProgressDialog(mContext, R.style.progress_dialog_bar)
        modifyProgressDialog(progressDialog)

        return v
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.btn_register -> {
                hideKeyboard()
                doValidations()
            }

            R.id.tv_continue_as_guest -> {
                hideKeyboard()
                activity?.supportFragmentManager?.popBackStack(
                    BACKSTACK_LOGIN_FRAGMENT,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }

            R.id.tv_goto_login -> {
                hideKeyboard()
                activity?.supportFragmentManager?.popBackStack()
            }
        }
    }

    private fun doValidations() {
        if (et_username.text.isEmpty()) {
            et_username.error = resources.getString(R.string.hint_username)
            et_username.requestFocus()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(et_email.text).matches()) {
            et_email.error = resources.getString(R.string.enter_valid_email)
            et_email.requestFocus()
        } else if (et_password.text.isEmpty()) {
            et_password.error = resources.getString(R.string.hint_password)
            et_password.requestFocus()
        } else if (et_password_confirm.text.isEmpty()) {
            et_password_confirm.error = resources.getString(R.string.hint_confirm_password)
            et_password_confirm.requestFocus()
        } else if (et_password.text.toString() != et_password_confirm.text.toString()) {
            et_password.error = resources.getString(R.string.passwords_no_match)
            et_password.requestFocus()
        } else if (et_phone.text.isEmpty()) {
            et_phone.error = resources.getString(R.string.hint_phone_number)
            et_phone.requestFocus()
        } else {
            // Register user
            Log.d(TAG, "Validations are done")
            progressDialog.show()
            register(
                et_username.text.toString(),
                et_email.text.toString(),
                et_password.text.toString(),
                et_phone.text.toString()
            )
        }
    }

    private fun register(
        username: String,
        email: String,
        password: String,
        phone: String
    ) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Registration completed!")
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    val ref =
                        FirebaseFirestore.getInstance()
                            .collection(Constants.FIRESTORE_USERS_REFERENCE).document(uid)

                    val user = User(
                        uid,
                        username,
                        phone,
                        0f,
                        Rent(false, null, null, 0)
                    )
                    ref.set(user)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d(TAG, "User added into firestore!")
                                if (!mAuth.currentUser?.isEmailVerified!!) {
                                    sendVerification()
                                } else {
                                    progressDialog.dismiss()
                                    activity?.supportFragmentManager?.popBackStack(
                                        BACKSTACK_LOGIN_FRAGMENT,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                                    )
                                }
                            } else {
                                progressDialog.dismiss()
                                makeToast(it.exception?.message.toString())
                            }
                        }
                } else {
                    progressDialog.dismiss()
                    makeToast(it.exception?.message.toString())
                }
            }

    }

    private fun sendVerification() {
        mAuth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener {
                progressDialog.dismiss()
                if (it.isSuccessful) {
                    makeToast(
                        resources.getString(R.string.verification_sent) + " " + mAuth.currentUser?.email,
                        Toast.LENGTH_LONG
                    )

                    mAuth.signOut()
                    activity?.supportFragmentManager?.popBackStack()
                } else makeToast(
                    resources.getString(R.string.verification_error) + " " + mAuth.currentUser?.email,
                    Toast.LENGTH_LONG
                )
            }
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onResume() {
        (activity as MainActivity).disableWidgets()
        super.onResume()
    }
}
