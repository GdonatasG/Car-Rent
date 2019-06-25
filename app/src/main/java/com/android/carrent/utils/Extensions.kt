package com.android.carrent.utils

import android.content.Context
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import com.android.carrent.R

fun setLogoAndFormFadeIn(context: Context, iv_logo: ImageView, form: LinearLayout) {
    val logoAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    logoAnimation.duration = Constants.LOGO_ANIMATION_DURATION

    val formAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    formAnimation.duration = Constants.FORM_ANIMATION_DURATION

    iv_logo.startAnimation(logoAnimation)
    form.startAnimation(formAnimation)
}