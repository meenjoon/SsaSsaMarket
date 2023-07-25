package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.data.model.FcmRequest
import com.mbj.ssassamarket.data.source.remote.MarketNotificationDataSource
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepository @Inject constructor(private val marketNotificationDataSource: MarketNotificationDataSource) {

    fun sendNotification(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        serverKey: String,
        notification: FcmRequest
    ): Flow<ApiResponse<Unit>> {
        return marketNotificationDataSource.sendNotification(onComplete, onError, serverKey, notification)
    }
}
