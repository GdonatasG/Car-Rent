package com.android.carrent.firestore.car

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

interface IFirestoreCarDao {
    fun getAllCars(): CollectionReference
}