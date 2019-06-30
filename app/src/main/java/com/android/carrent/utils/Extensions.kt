package com.android.carrent.utils

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.carrent.R

fun setLogoAndFormFadeIn(context: Context, iv_logo: ImageView, form: LinearLayout) {
    val logoAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    logoAnimation.duration = Constants.LOGO_ANIMATION_DURATION

    val formAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    formAnimation.duration = Constants.FORM_ANIMATION_DURATION

    iv_logo.startAnimation(logoAnimation)
    form.startAnimation(formAnimation)
}

fun Fragment.makeToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, duration).show()
}

fun showProgressBar(b: ProgressBar) {
    b.visibility = View.VISIBLE
}

fun hideProgressBar(b: ProgressBar) {
    b.visibility = View.INVISIBLE
}

fun Fragment.changeFragment(fragment: Fragment) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.replace(R.id.container, fragment)
        ?.commitAllowingStateLoss()
}