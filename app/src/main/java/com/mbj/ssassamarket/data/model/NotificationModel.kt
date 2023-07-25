package com.mbj.ssassamarket.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Notification(
    val type: String,
    val title: String,
    val body: String
)

@JsonClass(generateAdapter = true)
data class FcmRequest(
    val to: String,
    val priority: String,
    val data: Notification
)
