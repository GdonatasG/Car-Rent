package com.android.carrent.firestore.car

import com.android.carrent.models.Car.Car
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

interface IFirestoreCarDao {
    fun getAllCars(): CollectionReference
    fun getCarById(id: Int?): DocumentReference
    fun updateCar(car: Car?)
}