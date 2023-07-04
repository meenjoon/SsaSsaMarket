package com.mbj.ssassamarket.util

import android.content.Context
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.util.Constants.PATTERN_COMMA_THOUSANDS
import java.text.DecimalFormat

object TextFormat {

    fun convertToCurrencyFormat(price: Int, context: Context): String {
        val decimalFormat = DecimalFormat(PATTERN_COMMA_THOUSANDS)
        val formattedPrice = decimalFormat.format(price)
        val currencySymbol = context.getString(R.string.korean_currency)
        return "$formattedPrice$currencySymbol"
    }
}
