package com.mbj.ssassamarket.data.source.remote

import com.mbj.ssassamarket.data.model.FcmRequest
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface MarketNotificationDataSource {

    fun sendNotification(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        serverKey: String,
        notification: FcmRequest
    ): Flow<ApiResponse<Unit>>
}
