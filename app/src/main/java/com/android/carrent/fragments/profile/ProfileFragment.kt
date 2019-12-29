package com.android.carrent.fragments.profile


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.models.User.User
import com.android.carrent.utils.extensions.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    // ViewModel
    private lateinit var viewModel: ProfileViewModel

    // Context (to ensure that not null)
    private lateinit var mContext: Context

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private var flag = true

    private var user: MutableLiveData<User> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {

        // Init ViewModel
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            mAuth = it
            if (mAuth.currentUser == null && flag) {
                noUserRemoveFragment(ProfileFragment())
                flag = false
            }
        }

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View? = inflater.inflate(R.layout.fragment_profile, container, false)

        // Enable needed widgets
        (activity as MainActivity).enabledWidgets()

        setHasOptionsMenu(true)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        loadUser(mAuth.currentUser?.uid)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadUser(uid: String?) {
        uid?.let {
            viewModel.getUser(it).observe(viewLifecycleOwner, Observer<User> { u ->
                user.value = u

                setToolbarTitle(u.username + " " + resources.getString(R.string.profile))
                updateUserBalance(u.balance)
            })
        }
    }

    private fun updateUserBalance(amount: Float?) {
        amount?.let {
            tv_balance.text =
                String.format(
                    "%.2f",
                    amount
                ) + " " + resources.getString(R.string.nav_header_currency_euro)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context) {
        shouldShowHomeButton(activity, true)
        mContext = context
        super.onAttach(context)
    }

    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener!!)
        super.onStart()
    }

    override fun onDetach() {
        shouldShowHomeButton(activity, false)
        setToolbarTitle("")
        super.onDetach()
    }

    override fun onStop() {
        if (mAuthStateListener != null) FirebaseAuth.getInstance().removeAuthStateListener(
            mAuthStateListener!!
        )
        super.onStop()
    }
}
