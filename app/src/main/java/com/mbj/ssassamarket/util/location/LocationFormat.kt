package com.mbj.ssassamarket.util.location

import com.mbj.ssassamarket.util.Constants.KILOMETER_PATTERN
import kotlin.math.pow

object LocationFormat {

    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val radius = 6371 // 지구의 반지름 (단위: km)

        val latDiff = Math.toRadians(lat2 - lat1)
        val lonDiff = Math.toRadians(lon2 - lon1)

        val a = Math.sin(latDiff / 2)
            .pow(2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDiff / 2).pow(2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return radius * c // 두 지점 간의 거리 (단위: km)
    }

    fun formatDistance(distance: Double): String {
        return if (distance < 1.0) {
            val distanceInMeters = (distance * 1000).toInt()
            "$distanceInMeters m"
        } else {
            val distanceInKilometers = String.format(KILOMETER_PATTERN, distance)
            "$distanceInKilometers km"
        }
    }

    fun parseLatLng(latLng: String?): Pair<Double, Double>? {
        if (latLng != null) {
            val coordinates = latLng.split(" ")
            val latitude = coordinates.getOrNull(0)?.toDoubleOrNull()
            val longitude = coordinates.getOrNull(1)?.toDoubleOrNull()

            if (latitude != null && longitude != null) {
                return Pair(latitude, longitude)
            }
        }
        return null
    }
}
