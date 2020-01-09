package com.android.carrent.utils.extensions

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.fragments.authentication.LoginFragment
import com.android.carrent.utils.constants.Constants.BACKSTACK_LOGIN_FRAGMENT
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

fun Fragment.makeToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, duration).show()
}

fun Activity.makeToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(applicationContext, message, duration).show()
}

fun modifyProgressDialog(pd: ProgressDialog?) {
    pd?.setCancelable(false)
    pd?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

fun Fragment.changeFragmentWithBackstack(view: Int, fragment: Fragment, tag: String) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.replace(view, fragment)
        ?.addToBackStack(tag)
        ?.commit()
}

fun Fragment.addFragmentWithBackStack(
    view: Int,
    fragment: Fragment,
    tag: String
) {
    activity?.supportFragmentManager
        ?.beginTransaction()
        ?.add(view, fragment)
        ?.addToBackStack(tag)
        ?.commit()
}

fun Fragment.noUserPopFragment(message: String) {
    FirebaseAuth.getInstance().signOut()
    activity?.supportFragmentManager?.popBackStack()
    (activity as MainActivity).mSnackbar?.setText(message)
    (activity as MainActivity).mSnackbar?.show()
}

fun Fragment.noCarPopFragment(message: String) {
    activity?.supportFragmentManager?.popBackStack()
    (activity as MainActivity).mSnackbar?.setText(message)
    (activity as MainActivity).mSnackbar?.show()
}

fun Fragment.noUserGoToLogin(view: Int, context: Context?) {
    val snackbar = loginSnackbar(view, context)
    FirebaseAuth.getInstance().signOut()
    snackbar.show()
}

fun Fragment.loginSnackbar(view: Int, context: Context?): Snackbar {
    val snackbar = Snackbar.make(
        activity?.findViewById(R.id.connectivitySnack)!!,
        resources.getString(R.string.error_not_logged),
        Snackbar.LENGTH_LONG
    ).setAction(resources.getString(R.string.login)) {
        addFragmentWithBackStack(view, LoginFragment(), BACKSTACK_LOGIN_FRAGMENT)
    }
    snackbar.duration = BaseTransientBottomBar.LENGTH_LONG
    mainSnackbarView(snackbar = snackbar, context = context)

    return snackbar
}


fun mainSnackbarView(snackbar: Snackbar?, context: Context?) {
    val snack_root_view = snackbar?.view

    val snack_text_view = snack_root_view
        ?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

    val snack_action_view =
        snack_root_view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)

    snack_text_view?.typeface = Typeface.MONOSPACE
    snack_text_view?.maxLines = 2
    snack_text_view?.textSize = 12f

    snack_action_view?.setTextColor(context?.resources?.getColor(R.color.orange)!!)
}

fun shouldShowHomeButton(activity: Activity?, shouldShow: Boolean) {
    (activity as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(shouldShow)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(shouldShow)
}

fun Fragment.setToolbarTitle(title: String?) {
    (activity as MainActivity).setToolbarTitle(title)
}

fun Fragment.hideKeyboard() {
    val imm =
        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}

fun Fragment.detailCarColorBold(text: String?): String {

    return "<b><font color=" + resources.getColor(R.color.car_rented) + ">" + text + "</font></b>"
}
