package com.android.carrent.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.carrent.R
import com.android.carrent.fragments.LoginFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var loginFragment = LoginFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.container, loginFragment)
            .commit()
    }
}
