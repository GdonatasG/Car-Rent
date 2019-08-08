package com.android.carrent.firestore.car

import com.android.carrent.utils.Constants.FIRESTORE_CARS_REFERENCE
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreCarRepository: IFirestoreCarDao {
    override fun getAllCars(): CollectionReference {
        val ref = FirebaseFirestore.getInstance().collection(FIRESTORE_CARS_REFERENCE)

        return ref
    }
}