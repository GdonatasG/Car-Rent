package com.android.carrent.models.User

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

class Rent {
    @PropertyName("hasCar")
    var hasCar: Boolean? = null
    @PropertyName("rentStarted")
    var rentStarted: Timestamp? = null
    @PropertyName("rentedUntil")
    var rentedUntil: Timestamp? = null
    @PropertyName("rentedCarId")
    var rentedCarId: Int? = null

    constructor()

    constructor(
        hasCar: Boolean?,
        rentStarted: Timestamp?,
        rentedUntil: Timestamp?,
        rentedCarId: Int?
    ) {
        this.hasCar = hasCar
        this.rentStarted = rentStarted
        this.rentedUntil = rentedUntil
        this.rentedCarId = rentedCarId
    }
}