package com.mbj.ssassamarket.data.source.remote

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener
import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface MarketNetworkDataSource {

    fun addProductPost(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
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
    ): Flow<ApiResponse<Map<String, String>>>

    fun getUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, Map<String, User>>>>

    fun addUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        nickname: String
    ): Flow<ApiResponse<Map<String, String>>>

    fun updateMyLatLng(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        dataId: String,
        latLng: PatchUserLatLng
    ): Flow<ApiResponse<Unit>>

    fun getProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, ProductPostItem>>>

    fun updateProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: PatchProductRequest
    ): Flow<ApiResponse<Unit>>

    fun buyProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: PatchBuyRequest,
    ): Flow<ApiResponse<Unit>>

    fun enterChatRoom(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        otherUserId: String,
        otherUserName: String,
        otherLocation: String,
        createdChatRoom: String,
    ): Flow<ApiResponse<String>>

    fun getProductDetail(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String
    ): Flow<ApiResponse<ProductPostItem>>

    fun updateProductFavorite(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: FavoriteCountRequest
    ): Flow<ApiResponse<Unit>>

    fun getMyUserItem(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<User>>

    fun getOtherUserItem(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        userId: String
    ): Flow<ApiResponse<User>>

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
    ): Flow<ApiResponse<Unit>>

    fun getChatRooms(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<List<ChatRoomItem>>>

    fun logout(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Unit>>

    fun deleteUserData(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        uid: String
    ): Flow<ApiResponse<Unit>>

    fun deleteProductData(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postUid: String
    ): Flow<ApiResponse<Unit>>

    fun addChatDetailEventListener(chatRoomId: String, onChatItemAdded: (ChatItem) -> Unit): ChildEventListener

    fun removeChatDetailEventListener(chatDetailEventListener: ChildEventListener?, chatRoomId: String)

    suspend fun addChatRoomsValueEventListener(callback: (List<ChatRoomItem>) -> Unit): ValueEventListener

    suspend fun removeChatRoomsValueEventListener(valueEventListener: ValueEventListener?)

    suspend fun getDownloadUrl(imageLocation: String): String

    suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?>
}
