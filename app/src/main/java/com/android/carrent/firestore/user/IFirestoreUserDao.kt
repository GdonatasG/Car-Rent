package com.android.carrent.firestore.user

import com.google.firebase.firestore.DocumentReference

interface IFirestoreUserDao {
    fun getUser(uid: String): DocumentReference
}