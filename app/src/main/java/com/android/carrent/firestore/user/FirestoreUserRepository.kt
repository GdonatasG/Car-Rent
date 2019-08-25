package com.android.carrent.firestore.user

import com.android.carrent.utils.constants.Constants
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreUserRepository: IFirestoreUserDao {
    override fun getUser(uid: String): DocumentReference {
        val ref = FirebaseFirestore.getInstance().collection(Constants.FIRESTORE_USERS_REFERENCE).document(uid)
        return ref
    }

}