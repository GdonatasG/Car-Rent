package com.android.carrent.fragments.home.HomeFragment

import android.location.Location
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.android.carrent.firestore.car.FirestoreCarRepository
import com.android.carrent.firestore.user.FirestoreUserRepository
import com.android.carrent.models.Car.Car
import com.android.carrent.models.User
import com.android.carrent.utils.extensions.getDistance
import com.google.firebase.firestore.DocumentChange

class HomeFragmentViewModel : ViewModel() {
    private val TAG: String = "HomeFragmentViewModel"
    private var userRepository = FirestoreUserRepository()
    private var carRepository = FirestoreCarRepository()
    private var user: MutableLiveData<User> = MutableLiveData()

    fun getUser(uid: String): LiveData<User> {
        userRepository.getUser(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed of user.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val u = snapshot.toObject(com.android.carrent.models.User::class.java)
                    user.value = u
                } else {
                    Log.d(TAG, "Snapshot data: null")
                }
            }

        return user
    }

    fun getAllCars(): MutableList<Car> {
        val carList = mutableListOf<Car>()
        var existingList = mutableListOf<Car>()
        carRepository.getAllCars()
            .addSnapshotListener { s, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed of cars.", e)
                    return@addSnapshotListener
                }

                for (dc in s!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val c = dc.document.toObject(Car::class.java)
                            if (existingList.isEmpty()) {
                                carList.add(c)
                            } else {
                                for (car in existingList) {
                                    if (car.id == c.id) {
                                        carList.add(c)
                                    }
                                }
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val car = dc.document.toObject(Car::class.java)
                            carList.forEachIndexed { i, e ->
                                if (e.id == car.id) {
                                    carList[i] = car
                                }
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            val car = dc.document.toObject(Car::class.java)
                            carList.forEachIndexed { i, e ->
                                if (e.id == car.id) {
                                    carList.removeAt(i)
                                }
                            }
                        }
                    }
                }
                existingList = carList

            }

        return carList
    }

    fun sortCarList(list: MutableList<Car>?, deviceLocation: Location?) {
        list?.sortWith(compareBy {
            getDistance(
                deviceLocation!!.latitude,
                deviceLocation.longitude,
                it.location!!.latitude,
                it.location!!.longitude
            )
        })
    }

    fun rvListHandlerOnLocationSuccess(rv: RecyclerView?, pb: ProgressBar?) {
        rv?.visibility = View.VISIBLE
        pb?.visibility = View.GONE
    }


}