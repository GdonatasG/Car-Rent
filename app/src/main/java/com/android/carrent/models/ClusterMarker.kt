package com.android.carrent.models

import android.graphics.drawable.Drawable
import com.android.carrent.models.Car.Car
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterMarker(
    @get:JvmName("getSnippet_") var snippet: String, @get:JvmName("getTitle_") var title: String, @get:JvmName(
        "getPosition_") var position: LatLng, var icon: Int) : ClusterItem {
    override fun getSnippet(): String {
        return snippet
    }

    override fun getTitle(): String {
        return title
    }

    override fun getPosition(): LatLng {
        return position
    }




}