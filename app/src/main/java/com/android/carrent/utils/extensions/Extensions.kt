package com.android.carrent.utils.extensions

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.fragments.authentication.LoginFragment
import com.android.carrent.utils.constants.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


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

fun Fragment.clearBackStack() {
    activity?.supportFragmentManager?.popBackStack()
}

fun modifyProgressDialog(pd: ProgressDialog?) {
    pd?.setCancelable(false)
    pd?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

fun Fragment.changeFragment(view: Int, fragment: Fragment) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.replace(view, fragment)
        ?.commit()
}

fun Fragment.removeFragment(fragment: Fragment) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.remove(fragment)
        ?.commit()
}

fun Fragment.changeFragmentWithBackstack(view: Int, fragment: Fragment, tag: String) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.replace(view, fragment)
        ?.addToBackStack(tag)
        ?.commit()
}

fun Fragment.addFragmentWithBackStack(view: Int, fragment: Fragment, tag: String) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.add(view, fragment)
        ?.addToBackStack(tag)
        ?.commit()
}

fun Fragment.noUserRemoveFragment(fragment: Fragment, showMessage: Boolean = true) {
    FirebaseAuth.getInstance().signOut()
    clearBackStack()
    removeFragment(fragment)
    if (showMessage) makeToast(resources.getString(R.string.error_no_user))

}

fun Fragment.noUserGoToLogin(view: Int) {
    FirebaseAuth.getInstance().signOut()
    clearBackStack()
    changeFragment(view, LoginFragment())
    makeToast(resources.getString(R.string.error_no_user))
}


fun mainSnackbarView(snackbar: Snackbar?, activity: Activity?) {
    val snack_root_view = snackbar?.view

    val snack_text_view = snack_root_view
        ?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

    val snack_action_view =
        snack_root_view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)

    snack_text_view?.typeface = Typeface.MONOSPACE
    snack_text_view?.maxLines = 2
    snack_text_view?.textSize = 12f

    snack_action_view?.setTextColor(activity?.resources?.getColor(R.color.orange)!!)
}

fun shouldShowHomeButton(activity: Activity?, shouldShow: Boolean) {
    (activity as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(shouldShow)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(shouldShow)
}

fun Fragment.setToolbarTitle(title: String?) {
    (activity as MainActivity).setToolbarTitle(title)
}
