package com.android.carrent.firestore

import com.google.firebase.firestore.DocumentReference

interface IFirestoreUserDao {
    fun getUser(uid: String): DocumentReference
}