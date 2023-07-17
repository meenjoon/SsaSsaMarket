package com.mbj.ssassamarket.data.source

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener
import com.mbj.ssassamarket.data.model.ChatItem
import com.mbj.ssassamarket.data.model.ChatRoomItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

    fun enterChatRoom(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        ohterUserId: String,
        otherUserName: String,
        otherLocation: String
    ): Flow<ApiResponse<String>> {
        return marketNetworkDataSource.enterChatRoom(onComplete, onError, ohterUserId, otherUserName, otherLocation)
    }

    suspend fun getMyUserItem(): ApiResponse<User> {
        return marketNetworkDataSource.getMyUserItem()
    }

    suspend fun getOtherUserItem(userId: String): ApiResponse<User> {
        return marketNetworkDataSource.getOtherUserItem(userId)
    }

    suspend fun sendMessage(
        chatRoomId: String,
        otherUserId: String,
        message: String,
        myUserName: String,
        myLocation: String,
        lastSentTime: String,
        myLatLng: String,
        dataId: String
    ): ApiResponse<Unit> {
        return marketNetworkDataSource.sendMessage(
            chatRoomId,
            otherUserId,
            message,
            myUserName,
            myLocation,
            lastSentTime,
            myLatLng,
            dataId
        )
    }

    suspend fun getChatRooms(): ApiResponse<List<ChatRoomItem>> {
        return marketNetworkDataSource.getChatRooms()
    }

    fun addChatDetailEventListener(
        chatRoomId: String,
        onChatItemAdded: (ChatItem) -> Unit
    ): ChildEventListener {
        return marketNetworkDataSource.addChatDetailEventListener(chatRoomId, onChatItemAdded)
    }

    fun removeChatDetailEventListener(
        chatDetailEventListener: ChildEventListener?,
        chatRoomId: String
    ) {
        marketNetworkDataSource.removeChatDetailEventListener(chatDetailEventListener, chatRoomId)
    }

    suspend fun addChatRoomsValueEventListener(callback: (List<ChatRoomItem>) -> Unit): ValueEventListener {
        return marketNetworkDataSource.addChatRoomsValueEventListener(callback)
    }

    suspend fun removeChatRoomsValueEventListener(valueEventListener: ValueEventListener) {
        marketNetworkDataSource.removeChatRoomsValueEventListener(valueEventListener)
    }
}
