package com.mbj.ssassamarket.data.source.remote

import com.mbj.ssassamarket.data.model.FcmRequest
import com.mbj.ssassamarket.data.source.remote.network.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class FcmDataSource @Inject constructor(private val fcmClient: FcmClient) :
    MarketNotificationDataSource {

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    override fun sendNotification(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        serverKey: String,
        notification: FcmRequest
    ): Flow<ApiResponse<Unit>> = flow<ApiResponse<Unit>> {
        try {
            val response = fcmClient.sendNotification(serverKey, notification)

            response.onSuccess {
                emit(response)
            }.onError { code, message ->
                onError("code: $code, message: $message")
            }.onException { throwable ->
                onError(throwable.message)
            }
        } catch (e: Exception) {
            onError(e.message)
        }
    }.onCompletion {
        onComplete()
    }.flowOn(defaultDispatcher)
}
