package com.android.carrent.fragments.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.carrent.firebase.car.FirestoreCarRepository
import com.android.carrent.firebase.user.FirestoreUserRepository
import com.android.carrent.models.Car.Car
import com.android.carrent.models.User.User

class ProfileViewModel : ViewModel() {
    private val TAG: String = "ProfileViewModel"
    private var userRepository = FirestoreUserRepository()
    private var carRepository = FirestoreCarRepository()
    private var user: MutableLiveData<User> = MutableLiveData()
    private var car: MutableLiveData<Car> = MutableLiveData()

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
}