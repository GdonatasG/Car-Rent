package com.android.carrent.adapters

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.carrent.R
import com.android.carrent.models.Car.Car
import com.android.carrent.utils.extensions.getAddress
import com.android.carrent.utils.extensions.getDistance
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.car_layout.view.*

class CarAdapter(var carList: MutableList<Car>, var context: Context, var deviceLocation: Location? = null) :
    RecyclerView.Adapter<CarAdapter.CarHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.car_layout, parent, false)

        return CarHolder(view)
    }

    override fun getItemCount(): Int {
        return carList.size
    }

    override fun onBindViewHolder(holder: CarHolder, position: Int) {
        val car = carList[position]
        holder.setData(car, position)
    }

    fun updateAdapter(list: MutableList<Car>, deviceLocation: Location? = null) {
        deviceLocation?.let { this.deviceLocation = it }
        this.carList = list
        notifyDataSetChanged()
    }

    inner class CarHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var currentCar: Car? = null
        var currentPosition: Int = 0

        fun setData(car: Car?, pos: Int) {
            car?.let {
                itemView.tv_title.text = it.model.title

                //setCarAddressCity(it.location!!.latitude, it.location!!.longitude)
                setCarIconStatus(it.rent.rented)
                setCarDistance(it.location)
            }

            this.currentCar = car
            this.currentPosition = pos
        }

        private fun setCarIconStatus(rented: Boolean?) {

            if (rented!!)
                itemView.iv_status.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_directions_car_rented_24dp,
                        null
                    )
                )
            else
                itemView.iv_status.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_directions_car_free_24dp,
                        null
                    )
                )
        }

        private fun setCarDistance(carLocation: GeoPoint?) {
            itemView.tv_distance.text = String.format(
                "%.1f", getDistance(
                    deviceLocation!!.latitude,
                    deviceLocation!!.longitude,
                    carLocation!!.latitude,
                    carLocation.longitude
                )
            ) + " " + context.resources.getString(R.string.distance_value)
        }

        private fun setCarAddressCity(lat: Double, lng: Double) {
            itemView.tv_address.text =
                getAddress(
                lat = lat,
                lng = lng,
                context = context
            ).locality

        }

    }

}