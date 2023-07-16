package com.mbj.ssassamarket.data.source.remote

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener
import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface MarketNetworkDataSource {

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
    ): ApiResponse<Map<String, String>>
    fun getUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, Map<String, User>>>>
    fun addUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        nickname: String
    ): Flow<ApiResponse<Map<String, String>>>
    suspend fun updateMyLatLng(dataId: String, latLng: PatchUserLatLng): ApiResponse<Unit>
    suspend fun getProduct(): ApiResponse<Map<String, ProductPostItem>>
    suspend fun getProductDetail(postId: String): ApiResponse<ProductPostItem>
    suspend fun getUserNameByUserId(): ApiResponse<Map<String, Map<String, User>>>
    suspend fun updateProduct(postId: String, request: PatchProductRequest): ApiResponse<Unit>
    suspend fun updateProductFavorite(postId: String, request: FavoriteCountRequest): ApiResponse<Unit>
    suspend fun buyProduct(postId: String, request: PatchBuyRequest): ApiResponse<Unit>
    suspend fun enterChatRoom(productId: String, otherUserName: String, otherLocation: String): ApiResponse<String>
    suspend fun getMyUserItem() : ApiResponse<User>
    suspend fun sendMessage(chatRoomId: String, otherUserId: String, message: String, myUserName: String, myLocation: String, lastSentTime: String, myLatLng: String, dataId: String): ApiResponse<Unit>
    suspend fun getOtherUserItem(userId: String): ApiResponse<User>
    suspend fun getChatRooms(): ApiResponse<List<ChatRoomItem>>
    fun addChatDetailEventListener(chatRoomId: String, onChatItemAdded: (ChatItem) -> Unit): ChildEventListener
    fun removeChatDetailEventListener(chatDetailEventListener: ChildEventListener?, chatRoomId: String)
    suspend fun addChatRoomsValueEventListener(callback: (List<ChatRoomItem>) -> Unit): ValueEventListener
    suspend fun removeChatRoomsValueEventListener(valueEventListener: ValueEventListener?)
    suspend fun getDownloadUrl(imageLocation: String): String
    suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?>
}
