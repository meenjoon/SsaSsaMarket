package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import javax.inject.Inject

class ChatRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {
    suspend fun enterChatRoom(productId: String, otherUserName: String, otherLocation: String): String {
        return marketNetworkDataSource.enterChatRoom(productId, otherUserName, otherLocation)
    }
}
