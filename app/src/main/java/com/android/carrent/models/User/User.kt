package com.android.carrent.models.User

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

class User {
    @Exclude
    var id: String? = null
    var username: String? = null
    var email: String? = null
    var phone: String? = null
    var balance: Float? = null
    @PropertyName("rent")
    var rent: Rent? = null

    constructor() {}

    constructor(
        id: String?,
        username: String?,
        email: String?,
        phone: String?,
        balance: Float?,
        rent: Rent?
    ) {
        this.id = id
        this.username = username
        this.email = email
        this.phone = phone
        this.balance = balance
        this.rent = rent
    }

}