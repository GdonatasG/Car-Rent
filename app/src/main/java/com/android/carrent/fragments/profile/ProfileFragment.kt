package com.android.carrent.fragments.profile


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.utils.extensions.changeFragment
import com.android.carrent.utils.extensions.shouldShowHomeButton
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    // Firebase
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
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
        shouldShowHomeButton(activity, true)

        changeFragment(R.id.profile_fragment_host, ProfilePreferenceFragment())


        return v
    }

    override fun onDetach() {
        shouldShowHomeButton(activity, false)
        super.onDetach()
    }
}
