package com.android.carrent.models.Car

import com.google.firebase.firestore.PropertyName

class Model {
    @PropertyName("title")
    var title: String? = null
    @PropertyName("photoUrl")
    var photoUrl: String? = null
    @PropertyName("gasType")
    var gasType: String? = null
    @PropertyName("fullTankCapacity")
    var fullTankCapacity: Float? = null
    @PropertyName("tankLeft")
    var tankLeft: Float? = null
    @PropertyName("averageFuelConsCity")
    var averageFuelConsCity: Float? = null
    @PropertyName("averageFuelConsOut")
    var averageFuelConsOut: Float? = null
    @PropertyName("averageFuelConsMixed")
    var averageFuelConsMixed: Float? = null
    @PropertyName("engine")
    var engine: String? = null

    constructor() {}

    constructor(
        title: String?,
        photoUrl: String?,
        gasType: String?,
        fullTankCapacity: Float?,
        tankLeft: Float?,
        averageFuelConsCity: Float?,
        averageFuelConsOut: Float?,
        averageFuelConsMixed: Float?,
        engine: String?
    ) {
        this.title = title
        this.photoUrl = photoUrl
        this.gasType = gasType
        this.fullTankCapacity = fullTankCapacity
        this.tankLeft = tankLeft
        this.averageFuelConsCity = averageFuelConsCity
        this.averageFuelConsOut = averageFuelConsOut
        this.averageFuelConsMixed = averageFuelConsMixed
        this.engine = engine
    }
}