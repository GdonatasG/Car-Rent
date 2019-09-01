package com.android.carrent.utils


import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.android.carrent.R

class LayoutWeightAnimations(mapView: View, recyclerView: View, context: Context) {
    // Views
    private var mapAnimationWrapper = ViewWeightAnimationWrapper(mapView)
    private var recyclerAnimationWrapper = ViewWeightAnimationWrapper(recyclerView)

    private val PROPERTY_NAME_WEIGHT: String = "weight"
    private val ANIMATION_DURATION: Long = 800 // animation time in ms

    var mMapLayoutState = 0
    val MAP_LAYOUT_STATE_CONTRACTED = 0
    val MAP_LAYOUT_STATE_EXPANDED = 1

    private val DEFAULT_WEIGHT =
        ResourcesCompat.getFloat(context.resources, R.dimen.default_weight)
    private val MAP_WEIGHT_CHANGED = 95f
    private val RECYCLER_WEIGHT_CHANGED = 5f

    fun expandMapAnimation() {
        ObjectAnimator.ofFloat(mapAnimationWrapper, PROPERTY_NAME_WEIGHT, DEFAULT_WEIGHT, MAP_WEIGHT_CHANGED).apply {
            duration = ANIMATION_DURATION
            start()
        }

        ObjectAnimator.ofFloat(recyclerAnimationWrapper, PROPERTY_NAME_WEIGHT, DEFAULT_WEIGHT, RECYCLER_WEIGHT_CHANGED)
            .apply {
                duration = ANIMATION_DURATION
                start()
            }
    }

    fun contractMapAnimation() {
        ObjectAnimator.ofFloat(mapAnimationWrapper, PROPERTY_NAME_WEIGHT, MAP_WEIGHT_CHANGED, DEFAULT_WEIGHT).apply {
            duration = ANIMATION_DURATION
            start()
        }

        ObjectAnimator.ofFloat(recyclerAnimationWrapper, PROPERTY_NAME_WEIGHT, RECYCLER_WEIGHT_CHANGED, DEFAULT_WEIGHT)
            .apply {
                duration = ANIMATION_DURATION
                start()
            }
    }

}