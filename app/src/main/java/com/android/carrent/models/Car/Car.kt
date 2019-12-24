package com.android.carrent.models.Car

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

class Car {
    @Exclude
    var id: Int? = null
    var plateNumber: String? = null
    @PropertyName("location")
    var location: GeoPoint? = null
    @PropertyName("model")
    lateinit var model: Model
    @PropertyName("rate")
    lateinit var rate: Rate
    @PropertyName("rent")
    lateinit var rent: Rent

    constructor() {}

    constructor(
        id: Int?,
        plateNumber: String?,
        location: GeoPoint?,
        model: Model,
        rate: Rate,
        rent: Rent
    ) {
        this.id = id
        this.plateNumber = plateNumber
        this.location = location
        this.model = model
        this.rate = rate
        this.rent = rent
    }
}