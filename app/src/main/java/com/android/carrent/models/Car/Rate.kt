package com.android.carrent.models.Car

import com.google.firebase.firestore.PropertyName

class Rate {
    @PropertyName("hasDiscountWeekend")
    var hasDiscountWeekend: Boolean? = null
    @PropertyName("hasDiscountWorkdays")
    var hasDiscountWorkdays: Boolean? = null
    @PropertyName("discountPercentage")
    var discountPercentage: Float? = null
    @PropertyName("weekendRatePerH")
    var weekendRatePerH: Float? = null
    @PropertyName("workdaysRatePerH")
    var workdaysRatePerH: Float? = null

    constructor()

    constructor(
        hasDiscountWeekend: Boolean?,
        hasDiscountWorkdays: Boolean?,
        discountPercentage: Float?,
        weekendRatePerH: Float?,
        workdaysRatePerH: Float?
    ) {
        this.hasDiscountWeekend = hasDiscountWeekend
        this.hasDiscountWorkdays = hasDiscountWorkdays
        this.discountPercentage = discountPercentage
        this.weekendRatePerH = weekendRatePerH
        this.workdaysRatePerH = workdaysRatePerH
    }
}
