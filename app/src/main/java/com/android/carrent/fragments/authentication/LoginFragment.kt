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
import androidx.fragment.app.FragmentManager

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.utils.constants.Constants.BACKSTACK_LOGIN_FRAGMENT
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import com.android.carrent.utils.extensions.*
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment(), View.OnClickListener {
    private var TAG: String = "LoginFragment"

    private lateinit var mAuth: FirebaseAuth

    // Context (to ensure that not null)
    private lateinit var mContext: Context

    // ProgressDialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v: View = inflater.inflate(R.layout.fragment_login, container, false)

        v.btn_login.setOnClickListener(this)

        v.tv_continue_as_guest.setOnClickListener(this)

        v.tv_goto_register.setOnClickListener(this)

        v.tv_forgot_password.setOnClickListener(this)

        (activity as MainActivity).disableWidgets()

        // Init progress dialog
        progressDialog = ProgressDialog(mContext, R.style.progress_dialog_bar)
        modifyProgressDialog(progressDialog)

        return v
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> {
                Log.d(TAG, "Clicked on login button")
                doValidations()
            }

            R.id.tv_continue_as_guest -> {
                activity?.supportFragmentManager?.popBackStack(
                    BACKSTACK_LOGIN_FRAGMENT,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }

            R.id.tv_goto_register -> {
                Log.d(TAG, "Clicked on register textview")
                addFragmentWithBackStack(
                    view = R.id.container_host,
                    fragment = RegisterFragment(),
                    tag = BACKSTACK_LOGIN_FRAGMENT
                )
            }

            R.id.tv_forgot_password -> {
                Log.d(TAG, "Clicked on forgot password textview")
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
            progressDialog.show()
            login(et_email.text.toString(), et_password.text.toString())

        }
    }

    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (mAuth.currentUser?.isEmailVerified!!) {
                        Log.d(TAG, "User logged in")
                        progressDialog.dismiss()
                        activity?.supportFragmentManager?.popBackStack(
                            BACKSTACK_LOGIN_FRAGMENT,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    } else {
                        mAuth.signOut()
                        progressDialog.dismiss()
                        makeToast(resources.getString(R.string.not_verified))
                    }

                } else {
                    Log.d(TAG, "Something went wrong when logging in")
                    progressDialog.dismiss()
                    makeToast(it.exception?.message.toString())
                }
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

    override fun onDetach() {
        (activity as MainActivity).enabledWidgets()
        super.onDetach()
    }
}
