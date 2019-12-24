package com.android.carrent.fragments.profile


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View? = inflater.inflate(R.layout.fragment_profile, container, false)

        mAuth = FirebaseAuth.getInstance()

        // Enable needed widgets
        (activity as MainActivity).enabledWidgets()

        setHasOptionsMenu(true)

        changeFragment(R.id.profile_fragment_host, ProfilePreferenceFragment())

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context) {
        println("attached")
        shouldShowHomeButton(activity, true)
        super.onAttach(context)
    }

    override fun onDetach() {
        println("detached")
        shouldShowHomeButton(activity, false)
        super.onDetach()
    }
}
