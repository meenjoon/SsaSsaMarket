package com.mbj.ssassamarket.ui.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.util.Constants.CURRENT_DATE_PATTERN
import com.mbj.ssassamarket.util.Constants.PATTERN_COMMA_THOUSANDS
import com.mbj.ssassamarket.util.DateFormat.getCurrentTime
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

    val result = when {
        diffInYears > 0 -> "${diffInYears}년 전"
        diffInMonths > 0 -> "${diffInMonths}달 전"
        diffInDays > 0 -> "${diffInDays}일 전"
        diffInHours > 0 -> "${diffInHours}시간 전"
        diffInMinutes > 0 -> "${diffInMinutes}분 전"
        else -> "방금 전"
    }

    text = result
}

@BindingAdapter("priceFormatted")
fun setFormattedPrice(view: TextView, price: Int) {
    val context = view.context
    val decimalFormat = DecimalFormat(PATTERN_COMMA_THOUSANDS)
    val formattedPrice = decimalFormat.format(price)
    val currencySymbol = context.getString(R.string.korean_currency)
    view.text = "$formattedPrice $currencySymbol"
}
