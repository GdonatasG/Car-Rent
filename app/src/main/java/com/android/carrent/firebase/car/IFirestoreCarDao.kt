package com.android.carrent.firebase.car

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

interface IFirestoreCarDao {
    fun getAllCars(): CollectionReference
    fun getCarById(id: Int?): DocumentReference
}