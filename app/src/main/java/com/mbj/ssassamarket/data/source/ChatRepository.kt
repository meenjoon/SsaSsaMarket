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

    fun getMyUserItem(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<User>> {
        return marketNetworkDataSource.getMyUserItem(onComplete, onError)
    }

    fun getOtherUserItem(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        userId: String
    ): Flow<ApiResponse<User>> {
        return marketNetworkDataSource.getOtherUserItem(onComplete, onError, userId)
    }

    fun sendMessage(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        chatRoomId: String,
        otherUserId: String,
        message: String,
        myUserName: String,
        myLocation: String,
        lastSentTime: String,
        myLatLng: String,
        dataId: String,
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.sendMessage(
            onComplete,
            onError,
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
