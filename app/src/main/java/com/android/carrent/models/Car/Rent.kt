package com.android.carrent.models.Car

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

class Rent {
    @PropertyName("rented")
    var rented: Boolean? = null
    @PropertyName("rentStarted")
    var rentStarted: Timestamp? = null
    @PropertyName("rentedUntil")
    var rentedUntil: Timestamp? = null
    @PropertyName("rentedById")
    var rentedById: String? = null

    constructor() {}

    constructor(rented: Boolean?, rentStarted: Timestamp?, rentedUntil: Timestamp?, rentedById: String?) {
        this.rented = rented
        this.rentStarted = rentStarted
        this.rentedUntil = rentedUntil
        this.rentedById = rentedById
    }
}