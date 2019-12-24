package com.android.carrent.firestore.car

import com.android.carrent.models.Car.Car
import com.android.carrent.utils.constants.Constants.FIRESTORE_CARS_REFERENCE
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreCarRepository : IFirestoreCarDao {
    override fun updateCar(car: Car?) {
        val ref = FirebaseFirestore.getInstance().collection(FIRESTORE_CARS_REFERENCE)
            .document(car?.id.toString())

        ref.set(car!!)
    }

    override fun getCarById(id: Int?): DocumentReference {
        val ref = FirebaseFirestore.getInstance().collection(FIRESTORE_CARS_REFERENCE)
            .document(id.toString())
        return ref
    }

    override fun getAllCars(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(FIRESTORE_CARS_REFERENCE)
    }
}