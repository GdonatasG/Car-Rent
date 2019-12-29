package com.android.carrent.fragments.profile.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.carrent.firebase.user.FirestoreUserRepository
import com.android.carrent.models.User.User

class EditProfileViewModel : ViewModel() {
    private val TAG: String = "EditProfileViewModel"
    private var userRepository = FirestoreUserRepository()
    private var user: MutableLiveData<User> = MutableLiveData()

    fun getUser(uid: String): LiveData<User> {
        userRepository.getUser(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "getUser: Listen failed of user.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val u = snapshot.toObject(User::class.java)
                    user.value = u
                } else {
                    Log.d(TAG, "getUser: Snapshot data: null")
                }
            }

        return user
    }

    fun updateUser(user: User?) {
        userRepository.updateUser(user)
    }


}