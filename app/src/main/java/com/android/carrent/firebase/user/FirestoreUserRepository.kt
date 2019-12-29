package com.android.carrent.firebase.user

import com.android.carrent.models.User.User
import com.android.carrent.utils.constants.Constants
import com.android.carrent.utils.constants.Constants.FIRESTORE_USERS_REFERENCE
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreUserRepository : IFirestoreUserDao {
    override fun updateUser(user: User?) {
        val ref = FirebaseFirestore.getInstance().collection(FIRESTORE_USERS_REFERENCE)
            .document(user?.id!!)

        ref.set(user)
    }

    override fun getUser(uid: String): DocumentReference {
        val ref = FirebaseFirestore.getInstance().collection(Constants.FIRESTORE_USERS_REFERENCE)
            .document(uid)
        return ref
    }


}