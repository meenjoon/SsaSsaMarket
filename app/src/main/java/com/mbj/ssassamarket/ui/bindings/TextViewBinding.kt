package com.mbj.ssassamarket.ui.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.util.Constants.CURRENT_DATE_PATTERN
import com.mbj.ssassamarket.util.Constants.PATTERN_COMMA_THOUSANDS
import com.mbj.ssassamarket.util.DateFormat.getCurrentTime
import com.mbj.ssassamarket.util.TextFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("formattedElapsedTime")
fun TextView.setFormattedElapsedTime(createdDate: String) {
    val dateFormat = SimpleDateFormat(CURRENT_DATE_PATTERN, Locale.getDefault())

    val currentDate = dateFormat.parse(getCurrentTime())
    val startDate = dateFormat.parse(createdDate)

    val diffInMillis = currentDate.time - startDate.time

    val diffInSeconds = diffInMillis / 1000
    val diffInMinutes = diffInSeconds / 60
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24
    val diffInMonths = diffInDays / 30
    val diffInYears = diffInDays / 365

    val context = this.context
    val yearsAgo = context.getString(R.string.years_ago)
    val monthsAgo = context.getString(R.string.months_ago)
    val daysAgo = context.getString(R.string.days_ago)
    val hoursAgo = context.getString(R.string.hours_ago)
    val minutesAgo = context.getString(R.string.minutes_ago)
    val justNow = context.getString(R.string.just_now)

    val result = when {
        diffInYears > 0 -> "$diffInYears$yearsAgo"
        diffInMonths > 0 -> "$diffInMonths$monthsAgo"
        diffInDays > 0 -> "$diffInDays$daysAgo"
        diffInHours > 0 -> "$diffInHours$hoursAgo"
        diffInMinutes > 0 -> "$diffInMinutes$minutesAgo"
        else -> justNow
    }

    text = result
}

@BindingAdapter("priceFormatted")
fun TextView.setFormattedPrice(price: Int) {
    val context = this.context
    val decimalFormat = DecimalFormat(PATTERN_COMMA_THOUSANDS)
    val formattedPrice = decimalFormat.format(price)
    val currencySymbol = context.getString(R.string.korean_currency)
    text = "$formattedPrice $currencySymbol"
}

@BindingAdapter("initialLetter")
fun TextView.setInitialLetter(nickname: String) {
    text = TextFormat.getInitialLetter(nickname)
}
