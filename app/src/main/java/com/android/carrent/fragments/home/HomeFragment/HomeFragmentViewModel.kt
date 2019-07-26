package com.android.carrent.fragments.home.HomeFragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.carrent.firestore.FirestoreUserRepository
import com.android.carrent.models.User

class HomeFragmentViewModel : ViewModel() {
    private val TAG: String = "HomeFragmentViewModel"
    private var repository = FirestoreUserRepository()
    private var user: MutableLiveData<User> = MutableLiveData()

    fun getUser(uid: String): LiveData<User> {
        repository.getUser(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val u = snapshot.toObject(com.android.carrent.models.User::class.java)
                    user.value = u
                } else {
                    Log.d(TAG, "Snapshot data: null")
                }
            }

        return user
    }

}