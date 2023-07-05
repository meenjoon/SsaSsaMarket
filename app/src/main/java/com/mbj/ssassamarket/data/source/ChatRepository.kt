package com.mbj.ssassamarket.data.source

import com.google.firebase.database.ChildEventListener
import com.mbj.ssassamarket.data.model.ChatItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import javax.inject.Inject

class ChatRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

    suspend fun enterChatRoom(productId: String, otherUserName: String, otherLocation: String): String {
        return marketNetworkDataSource.enterChatRoom(productId, otherUserName, otherLocation)
    }

    suspend fun getMyUserItem(callback: (User) -> Unit) {
        marketNetworkDataSource.getMyUserItem(callback)
    }

    suspend fun getOtherUserItem(userId: String, callback: (User) -> Unit) {
        marketNetworkDataSource.getOtherUserItem(userId, callback)
    }

    suspend fun sendMessage(chatRoomId: String, otherUserId: String, message: String, otherUserName: String, otherLocation: String) {
        marketNetworkDataSource.sendMessage(chatRoomId, otherUserId, message, otherUserName, otherLocation)
    }

    suspend fun addChatDetailEventListener(
        chatRoomId: String,
        onChatItemAdded: (ChatItem) -> Unit
    ): ChildEventListener {
        return marketNetworkDataSource.addChatDetailEventListener(chatRoomId, onChatItemAdded)
    }

    suspend fun removeChatDetailEventListener(chatDetailEventListener: ChildEventListener?, chatRoomId: String) {
        marketNetworkDataSource.removeChatDetailEventListener(chatDetailEventListener, chatRoomId)
    }
}
