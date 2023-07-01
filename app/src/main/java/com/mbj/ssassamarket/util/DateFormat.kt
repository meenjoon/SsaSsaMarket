package com.mbj.ssassamarket.util

import android.os.Build
import com.mbj.ssassamarket.util.Constants.CURRENT_DATE_PATTERN
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Date

object DateFormat {
    fun getCurrentTime(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentLocalDateTime: LocalDateTime = LocalDateTime.now()
            val timeFormatter = DateTimeFormatter.ofPattern(CURRENT_DATE_PATTERN)
            val formattedTime = currentLocalDateTime.format(timeFormatter)
            formattedTime
        } else {
            val currentTimeMillis = System.currentTimeMillis()
            val currentTime = Date(currentTimeMillis)
            val timeFormat = SimpleDateFormat(CURRENT_DATE_PATTERN, Locale.getDefault())
            val formattedTime = timeFormat.format(currentTime)
            formattedTime
        }
    }
}
