package com.android.carrent.firebase.car

import com.android.carrent.utils.constants.Constants.FIRESTORE_CARS_REFERENCE
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreCarRepository : IFirestoreCarDao {

    override fun getCarById(id: Int?): DocumentReference {
        return FirebaseFirestore.getInstance().collection(FIRESTORE_CARS_REFERENCE)
            .document(id.toString())
    }

    override fun getAllCars(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(FIRESTORE_CARS_REFERENCE)
    }
}