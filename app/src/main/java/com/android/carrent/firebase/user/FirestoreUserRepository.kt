package com.android.carrent.firebase.user

import com.android.carrent.models.User.User
import com.android.carrent.utils.constants.Constants.FIRESTORE_USERS_REFERENCE
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreUserRepository : IFirestoreUserDao {

    override fun getUser(uid: String): DocumentReference {
        return FirebaseFirestore.getInstance().collection(FIRESTORE_USERS_REFERENCE)
            .document(uid)
    }




}