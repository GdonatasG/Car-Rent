package com.android.carrent.models.Car

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

class Rent {
    @PropertyName("isRented")
    var isRented: Boolean? = null
    @PropertyName("rentStarted")
    var rentStarted: Timestamp? = null
    @PropertyName("rentedUntil")
    var rentedUntil: Timestamp? = null
    @PropertyName("rentedById")
    var rentedById: String? = null

    constructor() {}

    constructor(isRented: Boolean?, rentStarted: Timestamp?, rentedUntil: Timestamp?, rentedById: String?) {
        this.isRented = isRented
        this.rentStarted = rentStarted
        this.rentedUntil = rentedUntil
        this.rentedById = rentedById
    }
}