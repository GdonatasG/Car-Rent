package com.android.carrent.fragments.CarManagement

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.carrent.R
import com.android.carrent.firebase.car.FirestoreCarRepository
import com.android.carrent.firebase.user.FirestoreUserRepository
import com.android.carrent.models.Car.Car
import com.android.carrent.models.ClusterMarker
import com.android.carrent.models.User.User
import com.android.carrent.utils.ClusterManagerRenderer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentReference
import com.google.maps.android.clustering.ClusterManager

class CarManagementViewModel : ViewModel() {

    private val TAG: String = "CarManagementViewModel"
    private var carRepository: FirestoreCarRepository = FirestoreCarRepository()
    private var userRepository: FirestoreUserRepository = FirestoreUserRepository()
    private var user: MutableLiveData<User> = MutableLiveData()
    private var car: MutableLiveData<Car> = MutableLiveData()

    // Marker clustering
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    private lateinit var mClusterManagerRenderer: ClusterManagerRenderer

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
                    user.value = null
                    Log.d(TAG, "getUser: Snapshot data: null")
                }
            }

        return user
    }

    fun getCarById(id: Int): LiveData<Car> {
        carRepository.getCarById(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "getCar: Listen failed of car.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val c = snapshot.toObject(Car::class.java)
                    car.value = c
                } else {
                    car.value = null
                    Log.d(TAG, "getCar: Snapshot data: null")
                }
            }

        return car
    }

    fun getUserRef(uid: String): DocumentReference {
        return userRepository.getUser(uid)
    }

    fun getCarRef(id: Int): DocumentReference {
        return carRepository.getCarById(id)
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