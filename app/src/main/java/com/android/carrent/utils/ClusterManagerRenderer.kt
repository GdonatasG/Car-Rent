package com.android.carrent.utils

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.android.carrent.models.ClusterMarker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


class ClusterManagerRenderer(context: Context?, map: GoogleMap?, clusterManager: ClusterManager<ClusterMarker>?) :
    DefaultClusterRenderer<ClusterMarker>(context, map, clusterManager) {

    private var iconGenerator: IconGenerator? = null
    private var imageView: ImageView? = null

    init {
        iconGenerator = IconGenerator(context)
        imageView = ImageView(context?.applicationContext)
        iconGenerator?.setContentView(imageView)
    }

    override fun onBeforeClusterItemRendered(item: ClusterMarker?, markerOptions: MarkerOptions?) {
        item?.let {
            imageView?.setImageResource(item.icon)
            val icon = iconGenerator?.makeIcon()

            markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))?.title(item.title)
                ?.snippet(item.snippet)
        }

    }

    override fun shouldRenderAsCluster(cluster: Cluster<ClusterMarker>?): Boolean {
        return false
    }
}
