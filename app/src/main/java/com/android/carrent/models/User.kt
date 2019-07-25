package com.android.carrent.models

import com.google.firebase.firestore.Exclude

class User {
    @Exclude
    var id: String? = null
    var username: String? = null
    var email: String? = null
    var phone: String? = null
    var balance: Float? = null
    var rentedCarId: Int? = null

    constructor() {}

    constructor(
        id: String?,
        username: String?,
        email: String?,
        phone: String?,
        balance: Float?,
        rentedCarId: Int?
    ) {
        this.id = id
        this.username = username
        this.email = email
        this.phone = phone
        this.balance = balance
        this.rentedCarId = rentedCarId
    }

}