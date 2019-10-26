package com.android.carrent.fragments.mycar


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth

class MyCarFragment : Fragment() {
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
        val v: View = inflater.inflate(R.layout.fragment_rented, container, false)

        // Enable needed widgets
        (activity as MainActivity).enabledWidgets()

        return v
    }
}
