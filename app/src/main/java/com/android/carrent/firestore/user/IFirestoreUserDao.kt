package com.android.carrent.firestore.user

import com.android.carrent.models.User.User
import com.google.firebase.firestore.DocumentReference

interface IFirestoreUserDao {
    fun getUser(uid: String): DocumentReference

    fun updateUser(user: User?)
}