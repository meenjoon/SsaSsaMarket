package com.mbj.ssassamarket.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "userId")
    val userId: String?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "latLng")
    val latLng: String?,
)
