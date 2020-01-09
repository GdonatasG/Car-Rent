package com.android.carrent.utils.extensions

import android.text.Html
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.android.carrent.R
import com.android.carrent.models.Car.Car
import com.android.carrent.utils.constants.Constants
import java.util.*
import java.util.concurrent.TimeUnit

fun Fragment.setFinalRentDateAndCost(
    tvDate: TextView,
    tvCost: TextView,
    totalSum: Float,
    isDateSelected: Boolean,
    isTimeSelected: Boolean,
    selectedDate: Calendar
) {
    if (isDateSelected and isTimeSelected) {
        tvDate.text =
            Html.fromHtml(
                detailCarColorBold(
                    Constants.DATE_FORMAT.format(
                        selectedDate.time
                    )
                )
            )

        tvCost.text =
            Html.fromHtml(
                detailCarColorBold(
                    String.format("%.2f", totalSum)
                ) + " " + resources.getString(R.string.nav_header_currency_euro)
            )
    }
}

fun calculateSum(ratePerH: Float, minutes: Long): Float {
    var sum = 0F
    var mins = minutes
    while (mins >= 60) {
        sum += ratePerH
        mins -= 60
    }

    if (mins > 0) {
        sum += mins * ratePerH / 60
    }

    return sum
}

fun calculateDiscountPercentage(per: Float): Float {
    val calculatedPercentage: Float
    if (per >= 1) {
        calculatedPercentage = per / 100
    } else if (per < 1 && per >= 0.01) {
        calculatedPercentage = per / 10
    } else {
        calculatedPercentage = per
    }
    return calculatedPercentage
}

fun isWeekend(): Boolean {
    return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || Calendar.getInstance().get(
        Calendar.DAY_OF_WEEK
    ) == Calendar.SUNDAY
}

fun totalRentSum(selectedDate: Calendar, car: MutableLiveData<Car>?): Float {
    val total: Float // Total sum
    val RENT_TIME_MINUTES =
        TimeUnit.MILLISECONDS.toMinutes(selectedDate.timeInMillis - Calendar.getInstance().timeInMillis)

    // Check weekend discount
    if (isWeekend()) {
        val sum = calculateSum(car?.value?.rate?.weekendRatePerH!!, RENT_TIME_MINUTES)
        if (car.value?.rate?.hasDiscountWeekend!!) total =
            sum - (sum * (calculateDiscountPercentage(car.value?.rate?.discountPercentage!!)))
        else total = sum
    }
    // Check workdays discount
    else {
        val sum = calculateSum(car?.value?.rate?.workdaysRatePerH!!, RENT_TIME_MINUTES)
        if (car.value?.rate?.hasDiscountWorkdays!!) total =
            sum - (sum * (calculateDiscountPercentage(car.value?.rate?.discountPercentage!!)))
        else total = sum
    }

    return total
}

fun isRentedOrHaveRented(car: MutableLiveData<Car>?, user: MutableLiveData<com.android.carrent.models.User.User>?): Boolean {
    return if (car?.value != null && user?.value != null) car.value?.rent?.rented!! || user.value?.rent?.hasCar!!
    else false
}