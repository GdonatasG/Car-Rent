package com.android.carrent.models

class User {
    var username: String? = null
    var email: String? = null
    var phone: String? = null
    var balance: Double? = null
    var rentedCarId: Int? = null

    constructor() {}

    constructor(username: String?, email: String?, phone: String?, balance: Double?, rentedCarId: Int?) {
        this.username = username
        this.email = email
        this.phone = phone
        this.balance = balance
        this.rentedCarId = rentedCarId
    }

}