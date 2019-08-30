package com.android.carrent.utils.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.utils.constants.Constants
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.view.*


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

fun Activity.makeToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(applicationContext, message, duration).show()
}

fun showProgressBar(b: ProgressBar?) {
    b?.visibility = View.VISIBLE
}

fun hideProgressBar(b: ProgressBar?) {
    b?.visibility = View.INVISIBLE
}

fun Fragment.changeFragment(fragment: Fragment) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.replace(R.id.container_host, fragment)
        ?.commitAllowingStateLoss()
}

fun mainSnackbarView(snackbar: Snackbar?) {
    val snack_root_view = snackbar?.view

    val snack_text_view = snack_root_view
        ?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

    snack_text_view?.typeface = Typeface.MONOSPACE
    snack_text_view?.maxLines = 2
    snack_text_view?.textSize = 12f
}
