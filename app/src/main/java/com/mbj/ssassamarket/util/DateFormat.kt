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

    fun getFormattedElapsedTime(createdDate: String): String {
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

        return result
    }
}
