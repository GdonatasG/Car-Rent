package com.android.carrent.utils

import android.view.View
import android.widget.LinearLayout

class ViewWeightAnimationWrapper(var view: View) {

    fun setWeight(weight: Float) {
        val params = view.layoutParams as LinearLayout.LayoutParams
        params.weight = weight
        view.parent.requestLayout()
    }

    fun getWeight(): Float {
        return (view.layoutParams as LinearLayout.LayoutParams).weight
    }

}