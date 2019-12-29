package com.android.carrent.fragments.profile.edit


import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.models.User.User
import com.android.carrent.utils.extensions.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_edit_profile.*

class EditProfileFragment : Fragment(), View.OnClickListener {

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null

    // Context (to ensure that not null)
    private lateinit var mContext: Context

    // ViewModel
    private lateinit var viewModel: EditProfileViewModel
    private var user: MutableLiveData<User> = MutableLiveData()

    // ProgressDialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable needed widgets
        (activity as MainActivity).enabledWidgets()
        setHasOptionsMenu(true)
        shouldShowHomeButton(activity, true)

        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            mAuth = it
        }

        viewModel = ViewModelProviders.of(this).get(EditProfileViewModel::class.java)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadUser(mAuth.currentUser?.uid)


        // Init progress dialog
        progressDialog = ProgressDialog(mContext, R.style.progress_dialog_bar)
        modifyProgressDialog(progressDialog)

        btn_confirm.setOnClickListener(this)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadUser(uid: String?) {
        uid?.let {
            viewModel.getUser(it).observe(viewLifecycleOwner, Observer<User> { u ->
                user.value = u
                setToolbarTitle(u.username + " " + resources.getString(R.string.profile))
                loadInputs(u)
            })
        }
    }

    private fun loadInputs(u: User) {
        et_username.setText(u.username)
        et_phone.setText(u.phone)
        et_email.setText(mAuth.currentUser?.email)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_confirm -> {
                if ((activity as MainActivity).isInternetOn) {
                    mAuth.currentUser?.reload()
                    if (mAuth.currentUser != null) updateCurrentUser()
                    else {
                        // ProfileFragment is still in the background
                        // Message that user is not logged in will appear from ProfileFragment AuthStateListener
                        noUserRemoveFragment(fragment = this, showMessage = false)
                    }
                } else (activity as MainActivity).showNetworkMessage()
            }
        }
    }

    private fun updateCurrentUser() {
        if (et_username.text.isEmpty()) {
            et_username.error = resources.getString(R.string.hint_username)
            et_username.requestFocus()
        } else if (et_phone.text.isEmpty()) {
            et_phone.error = resources.getString(R.string.hint_phone_number)
            et_phone.requestFocus()
        } else if (et_current_password.text.isEmpty()) {
            et_current_password.error = resources.getString(R.string.hint_enter_current_pass)
            et_current_password.requestFocus()
        } else if (et_new_password.text.toString() != et_repeat_new_password.text.toString()) {
            et_new_password.error = resources.getString(R.string.passwords_no_match)
            et_new_password.requestFocus()

            et_repeat_new_password.error = resources.getString(R.string.passwords_no_match)
            et_repeat_new_password.requestFocus()
        } else {
            user.value?.username = et_username.text.toString()
            user.value?.phone = et_phone.text.toString()
            progressDialog.show()


            mAuth.currentUser?.reload()
            if (mAuth.currentUser != null) {
                mAuth.currentUser?.reauthenticate(
                    EmailAuthProvider.getCredential(
                        mAuth.currentUser?.email!!,
                        et_current_password.text.toString()
                    )
                )?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Updating user record in Firestore
                        viewModel.updateUser(user.value)

                        // Updating password
                        if (et_new_password.text.isNotEmpty()) {
                            mAuth.currentUser?.updatePassword(et_new_password.text.toString())
                                ?.addOnCompleteListener {
                                    progressDialog.dismiss()
                                    if (it.isSuccessful) {
                                        clearPasswordInputs()
                                        makeToast(resources.getString(R.string.PROFILE_UPDATED))
                                    } else {
                                        makeToast(resources.getString(R.string.PASSWORD_UPDATE_ERROR))
                                    }
                                }
                        } else {
                            progressDialog.dismiss()
                            clearPasswordInputs()
                            makeToast(resources.getString(R.string.PROFILE_UPDATED))
                        }

                    } else {
                        progressDialog.dismiss()
                        makeToast(resources.getString(R.string.ERROR_WRONG_PASS))
                    }
                }
            } else {
                // ProfileFragment is still in the background
                // Message that user is not logged in will appear from ProfileFragment AuthStateListener
                noUserRemoveFragment(fragment = this, showMessage = false)
            }
        }

    }

    private fun clearPasswordInputs() {
        et_current_password.text.clear()
        et_new_password.text.clear()
        et_repeat_new_password.text.clear()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu.clear()
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onDetach() {
        shouldShowHomeButton(activity, false)
        setToolbarTitle("")
        super.onDetach()
    }

    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener!!)
        super.onStart()
    }

    override fun onStop() {
        if (mAuthStateListener != null) FirebaseAuth.getInstance().removeAuthStateListener(
            mAuthStateListener!!
        )
        super.onStop()
    }
}
