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
        otherUserId: String,
        otherUserName: String,
        otherLocation: String,
        createdChatRoom: String,
    ): Flow<ApiResponse<String>> {
        return marketNetworkDataSource.enterChatRoom(onComplete, onError, otherUserId, otherUserName, otherLocation, createdChatRoom)
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

    fun getMyChatRooms(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<List<ChatRoomItem>>> {
        return marketNetworkDataSource.getMyChatRoom(onComplete, onError)
    }

    fun getAllChatRoomData(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
    ): Flow<ApiResponse<Map<String, Map<String, ChatRoomItem>>>> {
        return marketNetworkDataSource.getAllChatRoomData(onComplete, onError)
    }

    fun deleteChatRoomsDataForMyUid(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        uid: String
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.deleteChatRoomsDataForMyUid(onComplete, onError, uid)
    }

    fun addChatDetailEventListener(
        chatRoomId: String,
        onChatItemAdded: (ChatItem) -> Unit
    ): ChildEventListener {
        return marketNetworkDataSource.addChatDetailEventListener(chatRoomId, onChatItemAdded)
    }

    fun deleteMyInfoFromChatRoomsForOtherUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        otherUid: String,
        myUid: String,
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.deleteMyInfoFromChatRoomsForOtherUser(onComplete, onError, otherUid, myUid)
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
