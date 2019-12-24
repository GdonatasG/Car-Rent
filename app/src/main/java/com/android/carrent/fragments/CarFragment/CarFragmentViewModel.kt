package com.android.carrent.fragments.CarFragment

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.android.carrent.R
import com.android.carrent.firestore.car.FirestoreCarRepository
import com.android.carrent.firestore.user.FirestoreUserRepository
import com.android.carrent.models.Car.Car
import com.android.carrent.models.ClusterMarker
import com.android.carrent.models.User.User
import com.android.carrent.utils.ClusterManagerRenderer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager

class CarFragmentViewModel : ViewModel() {
    private val TAG: String = "CarFragmentViewModel"

    private var car: MutableLiveData<Car> = MutableLiveData()
    private var user: MutableLiveData<User> = MutableLiveData()

    private var carRepository = FirestoreCarRepository()
    private var userRepository = FirestoreUserRepository()

    // Marker clustering
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    private lateinit var mClusterManagerRenderer: ClusterManagerRenderer

    fun getCarById(id: Int?): LiveData<Car> {
        carRepository.getCarById(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "getCarById: Listen failed of car.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val c = snapshot.toObject(Car::class.java)
                    car.value = c
                } else {
                    Log.d(TAG, "getCarById: Snapshot data: null")
                }

            }

        return car
    }

    fun getUser(uid: String): LiveData<User> {
        userRepository.getUser(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "getUser: Listen failed of user.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val u = snapshot.toObject(User::class.java)
                    user.value = u
                } else {
                    Log.d(TAG, "getUser: Snapshot data: null")
                }
            }
        return user
    }

    fun updateUser(user: User?) {
        userRepository.updateUser(user)
    }

    fun updateCar(car: Car?) {
        carRepository.updateCar(car)
    }

    fun handleExpandableView(expandableView: View?, cardView: ViewGroup?, arrowButton: Button?) {
        if (expandableView?.visibility == View.GONE) {
            cardView?.let {
                TransitionManager.beginDelayedTransition(
                    it,
                    AutoTransition()
                )
            }
            expandableView.visibility = View.VISIBLE
            arrowButton?.setBackgroundResource(
                R.drawable.ic_keyboard_arrow_up_lightgray
            )
        } else {
            cardView?.let {
                TransitionManager.beginDelayedTransition(
                    it,
                    AutoTransition()
                )
            }
            expandableView?.visibility = View.GONE
            arrowButton?.setBackgroundResource(
                R.drawable.ic_keyboard_arrow_down_lightgray
            )
        }
    }

    fun addMapMarker(map: GoogleMap, car: Car, context: Context?) {
        map.let {
            mClusterManager = ClusterManager(context, map)

            mClusterManagerRenderer = ClusterManagerRenderer(context, map, mClusterManager)

            mClusterManager.renderer = mClusterManagerRenderer

            mClusterManager.clearItems()
            map.clear()

            val icon =
                if (car.rent.rented!!) R.drawable.ic_directions_car_rented_24dp else R.drawable.ic_directions_car_free_24dp

            val marker =
                ClusterMarker(
                    "",
                    "",
                    LatLng(car.location!!.latitude, car.location!!.longitude),
                    icon
                )
            mClusterManager.addItem(marker)
            mClusterManager.cluster()

        }
    }

}