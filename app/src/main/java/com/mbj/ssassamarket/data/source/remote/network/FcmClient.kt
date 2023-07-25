package com.mbj.ssassamarket.data.source.remote.network

import com.mbj.ssassamarket.data.model.FcmRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmClient {

    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") serverKey: String,
        @Body fcmRequest: FcmRequest
    ): ApiResponse<Unit>
}
