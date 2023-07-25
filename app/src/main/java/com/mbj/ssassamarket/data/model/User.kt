package com.mbj.ssassamarket.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "userId")
    val userId: String? = null,
    @Json(name = "userName")
    val userName: String? = null,
    @Json(name = "latLng")
    val latLng: String? = null,
    @Json(name = "fcmToken")
    val fcmToken: String? = null
)

data class PatchUserLatLng(
    @Json(name = "latLng")
    val latLng: String? = null
)
