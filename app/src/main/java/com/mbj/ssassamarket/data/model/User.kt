package com.mbj.ssassamarket.data.model

import com.squareup.moshi.Json

data class User(
    @Json(name = "userId")
    val userId: String?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "latitudeAndLongitude")
    val latitudeAndLongitude: String?,
)
