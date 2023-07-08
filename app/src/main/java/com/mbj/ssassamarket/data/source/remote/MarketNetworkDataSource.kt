package com.mbj.ssassamarket.data.source.remote

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener
import com.mbj.ssassamarket.data.model.*

interface MarketNetworkDataSource {
    suspend fun currentUserExists(): Boolean
    suspend fun addUser(nickname: String): Boolean
    suspend fun checkDuplicateUserName(nickname: String): Boolean
    suspend fun addProductPost(
        content: String,
        imageLocations: List<ImageContent>,
        price: Int,
        title: String,
        category: String,
        soldOut: Boolean,
        favoriteCount: Int,
        shoppingList: List<String?>,
        location: String,
        latLng: String,
        favoriteList: List<String?>
    ): Boolean
    suspend fun getMyDataId(): String?
    suspend fun updateMyLatLng(latLng: String): Boolean
    suspend fun getProduct(): List<Pair<String, ProductPostItem>>
    suspend fun getUserAndIdToken() : Pair<FirebaseUser?, String?>
    suspend fun getUserNameByUserId(userIdToken: String) : String?
    suspend fun updateProduct(postId: String, request: PatchProductRequest)
    suspend fun enterChatRoom(productId: String, otherUserName: String, otherLocation: String): String
    suspend fun getMyUserItem(callback: (User) -> Unit)
    suspend fun sendMessage(chatRoomId: String, otherUserId: String, message: String, myUserName: String, myLocation: String, lastSentTime: String)
    suspend fun getOtherUserItem(userId: String, callback: (User) -> Unit)
    suspend fun getChatRooms(callback: (List<ChatRoomItem>) -> Unit)
    fun addChatDetailEventListener(chatRoomId: String, onChatItemAdded: (ChatItem) -> Unit): ChildEventListener
    fun removeChatDetailEventListener(chatDetailEventListener: ChildEventListener?, chatRoomId: String)
    suspend fun addChatRoomsValueEventListener(callback: (List<ChatRoomItem>) -> Unit): ValueEventListener
    suspend fun removeChatRoomsValueEventListener(valueEventListener: ValueEventListener?)
}
