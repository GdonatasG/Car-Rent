package com.android.carrent.models.Car

import com.google.firebase.firestore.PropertyName

class Model {
    @PropertyName("title")
    var title: String? = null
    @PropertyName("photoUrl")
    var photoUrl: String? = null
    @PropertyName("releaseDate")
    var releaseDate: String? = null
    @PropertyName("gasType")
    var gasType: String? = null
    @PropertyName("locked")
    var locked: Boolean? = null
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
        releaseDate: String?,
        gasType: String?,
        locked: Boolean?,
        fullTankCapacity: Float?,
        tankLeft: Float?,
        averageFuelConsCity: Float?,
        averageFuelConsOut: Float?,
        averageFuelConsMixed: Float?,
        engine: String?
    ) {
        this.title = title
        this.photoUrl = photoUrl
        this.releaseDate = releaseDate
        this.gasType = gasType
        this.locked = locked
        this.fullTankCapacity = fullTankCapacity
        this.tankLeft = tankLeft
        this.averageFuelConsCity = averageFuelConsCity
        this.averageFuelConsOut = averageFuelConsOut
        this.averageFuelConsMixed = averageFuelConsMixed
        this.engine = engine
    }


}