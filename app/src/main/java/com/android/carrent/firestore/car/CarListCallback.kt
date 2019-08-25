package com.android.carrent.firestore.car

import com.android.carrent.models.Car.Car

interface CarListCallback {
    fun onCarListCallback(carList: MutableList<Car>)
}