package com.mbj.ssassamarket.data.source.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mbj.ssassamarket.BuildConfig
import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.remote.network.*
import com.mbj.ssassamarket.util.Constants.CHATS
import com.mbj.ssassamarket.util.Constants.CHAT_ROOMS
import com.mbj.ssassamarket.util.Constants.CHAT_ROOM_ID
import com.mbj.ssassamarket.util.Constants.LAST_MESSAGE
import com.mbj.ssassamarket.util.Constants.LAST_SENT_TIME
import com.mbj.ssassamarket.util.Constants.LAT_LNG
import com.mbj.ssassamarket.util.Constants.OTHER_LOCATION
import com.mbj.ssassamarket.util.Constants.OTHER_USER_ID
import com.mbj.ssassamarket.util.Constants.OTHER_USER_NAME
import com.mbj.ssassamarket.util.Constants.USER
import com.mbj.ssassamarket.util.Constants.USER_ID
import com.mbj.ssassamarket.util.Constants.USER_NAME
import com.mbj.ssassamarket.util.DateFormat.getCurrentTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val apiClient: ApiClient,
    private val storage: FirebaseStorage
) : MarketNetworkDataSource {

    private val chatRoomsRef =
        Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference.child(CHAT_ROOMS)
    private val chatRef = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference.child(CHATS)
    private val database = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    override fun getUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, Map<String, User>>>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val googleIdToken = idToken ?: ""
        val response = apiClient.getUser(googleIdToken)
        response.onSuccess {
            emit(response)
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException {
            onError(it.message)
        }
    }.onCompletion {
        onComplete()
    }.flowOn(defaultDispatcher)

    override fun addUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        nickname: String
    ): Flow<ApiResponse<Map<String, String>>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val googleIdToken = idToken ?: ""
        val userItem = User(user?.uid, nickname, null)
        val response = apiClient.addUser(user?.uid ?: "", userItem, googleIdToken)

        response.onSuccess { data ->
            emit(response)
            onComplete()
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException { throwable ->
            onError(throwable.message)
        }
    }.flowOn(defaultDispatcher)

    override fun addProductPost(
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
    ): Flow<ApiResponse<Map<String, String>>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val uId = user?.uid ?: ""
        val googleIdToken = idToken ?: ""
        val productPostItem = ProductPostItem(
            uId,
            content,
            getCurrentTime(),
            uploadImages(imageLocations),
            price,
            title,
            category,
            soldOut,
            favoriteCount,
            shoppingList,
            location,
            latLng,
            favoriteList
        )
        val response = apiClient.addProductPost(productPostItem, googleIdToken)

        response.onSuccess { data ->
            emit(response)
            onComplete()
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException { throwable ->
            onError(throwable.message)
        }
    }.flowOn(defaultDispatcher)

    override fun updateMyLatLng(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        dataId: String,
        latLng: PatchUserLatLng
    ): Flow<ApiResponse<Unit>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val googleIdToken = idToken ?: ""
        val userId = user?.uid ?: ""
        val response = apiClient.updateMyLatLng(userId, dataId, latLng, googleIdToken)

        response.onSuccess {
            emit(response)
            onComplete()
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException { throwable ->
            onError(throwable.message)
        }
    }.flowOn(defaultDispatcher)

    override fun getProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, ProductPostItem>>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val googleIdToken = idToken ?: ""
        val response = apiClient.getProduct(googleIdToken)

        response.onSuccess {
            emit(response)
            onComplete()
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException { throwable ->
            onError(throwable.message)
        }
    }.flowOn(defaultDispatcher)

    override fun updateProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: PatchProductRequest
    ): Flow<ApiResponse<Unit>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val googleIdToken = idToken ?: ""
        val response = apiClient.updateProduct(postId, request, googleIdToken)

        response.onSuccess {
            emit(response)
            onComplete()
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException { throwable ->
            onError(throwable.message)
        }
    }.flowOn(defaultDispatcher)

    override fun buyProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: PatchBuyRequest,
    ): Flow<ApiResponse<Unit>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val googleIdToken = idToken ?: ""
        val response = apiClient.buyProduct(postId, request, googleIdToken)

        response.onSuccess {
            emit(response)
            onComplete()
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException { throwable ->
            onError(throwable.message)
        }
    }.flowOn(defaultDispatcher)

    override fun enterChatRoom(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        ohterUserId: String,
        otherUserName: String,
        otherLocation: String
    ): Flow<ApiResponse<String>> = flow {
        val userId = getUserAndIdToken().first?.uid ?: ""

        val chatRoomDB = chatRoomsRef.child(userId).child(ohterUserId)
        val dataSnapshot = chatRoomDB.get().await()

        if (dataSnapshot.value != null) {
            val chatRoom = dataSnapshot.getValue(ChatRoomItem::class.java)
            val chatRoomId = chatRoom?.chatRoomId ?: ""
            emit(ApiResultSuccess(chatRoomId))
            onComplete()
        } else {
            try {
                val chatRoomId = UUID.randomUUID().toString()
                val newChatRoom = ChatRoomItem(
                    chatRoomId = chatRoomId,
                    otherUserId = ohterUserId,
                    otherUserName = otherUserName,
                    otherLocation = otherLocation,
                )
                chatRoomDB.setValue(newChatRoom).await()
                emit(ApiResultSuccess(chatRoomId))
                onComplete()
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }.flowOn(defaultDispatcher)

    override fun getProductDetail(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String
    ): Flow<ApiResponse<ProductPostItem>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val googleIdToken = idToken ?: ""
        val response = apiClient.getProductDetail(postId, googleIdToken)

        response.onSuccess { productItem ->
            emit(ApiResultSuccess(productItem))
            onComplete()
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException { throwable ->
            onError(throwable.message)
        }
    }.flowOn(defaultDispatcher)

    override fun updateProductFavorite(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: FavoriteCountRequest
    ): Flow<ApiResponse<Unit>> = flow {
        val (user, idToken) = getUserAndIdToken()
        val googleIdToken = idToken ?: ""
        val response = apiClient.updateProductFavorite(postId, request, googleIdToken)

        response.onSuccess {
            emit(ApiResultSuccess(Unit))
            onComplete()
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException { throwable ->
            onError(throwable.message)
        }
    }.flowOn(defaultDispatcher)

    override suspend fun getMyUserItem(): ApiResponse<User> {
        val userId = getUserAndIdToken().first?.uid ?: ""

        return try {
            val dataSnapshot = database.child(USER).child(userId).get().await()

            for (childSnapshot in dataSnapshot.children) {
                val myUserId = childSnapshot.child(USER_ID).getValue(String::class.java)
                val myUserName = childSnapshot.child(USER_NAME).getValue(String::class.java)
                val myLatLng = childSnapshot.child(LAT_LNG).getValue(String::class.java)

                val myUserItem = User(myUserId, myUserName, myLatLng)
                return ApiResultSuccess(myUserItem)
            }
            ApiResultError(code = 400, message = "My User data not found")
        } catch (e: Exception) {
            ApiResultError(code = 500, message = e.message ?: "Failed to get my user")
        }
    }

    override suspend fun getOtherUserItem(userId: String): ApiResponse<User> {
        return try {
            val dataSnapshot = database.child(USER).child(userId).get().await()
            val user = dataSnapshot.children.mapNotNull { childSnapshot ->
                val otherUserId = childSnapshot.child(USER_ID).getValue(String::class.java)
                val otherUserName = childSnapshot.child(USER_NAME).getValue(String::class.java)
                val otherLatLng = childSnapshot.child(LAT_LNG).getValue(String::class.java)

                if (otherUserId != null && otherUserName != null && otherLatLng != null) {
                    User(otherUserId, otherUserName, otherLatLng)
                } else {
                    null
                }
            }.firstOrNull()

            if (user != null) {
                ApiResultSuccess(user)
            } else {
                ApiResultError(code = 400, message = "Other User data not found")
            }
        } catch (e: Exception) {
            ApiResultError(code = 500, message = e.message ?: "Failed to get other user")
        }
    }

    override suspend fun sendMessage(
        chatRoomId: String,
        otherUserId: String,
        message: String,
        myUserName: String,
        myLocation: String,
        lastSentTime: String,
        myLatLng: String,
        dataId: String
    ): ApiResponse<Unit> {
        return try {
            val userId = getUserAndIdToken().first?.uid ?: ""

            val newChatItem = ChatItem(
                message = message,
                userId = userId,
                lastSentTime = lastSentTime
            )

            newChatItem.chatId = database.child(CHATS).child(chatRoomId).push().key
            database.child(CHATS).child(chatRoomId).child(newChatItem.chatId ?: "")
                .setValue(newChatItem)

            val uId = getUserAndIdToken().first?.uid ?: ""

            val updates: MutableMap<String, Any> = hashMapOf(
                "${USER}/${uId}/${dataId}/${LAT_LNG}" to myLatLng,
                "${CHAT_ROOMS}/${userId}/$otherUserId/${LAST_MESSAGE}" to message,
                "${CHAT_ROOMS}/${userId}/$otherUserId/${LAST_SENT_TIME}" to lastSentTime,
                "${CHAT_ROOMS}/$otherUserId/${userId}/${LAST_MESSAGE}" to message,
                "${CHAT_ROOMS}/$otherUserId/${userId}/${LAST_SENT_TIME}" to lastSentTime,
                "${CHAT_ROOMS}/$otherUserId/${userId}/${CHAT_ROOM_ID}" to chatRoomId,
                "${CHAT_ROOMS}/$otherUserId/${userId}/${OTHER_USER_ID}" to userId,
                "${CHAT_ROOMS}/$otherUserId/${userId}/${OTHER_USER_NAME}" to myUserName,
                "${CHAT_ROOMS}/$otherUserId/${userId}/${OTHER_LOCATION}" to myLocation,
            )
            database.updateChildren(updates)

            ApiResultSuccess(Unit)
        } catch (e: Exception) {
            ApiResultError(code = 400, message = e.message ?: "Failed to send message")
        }
    }

    override suspend fun getChatRooms(): ApiResponse<List<ChatRoomItem>> {
        val userId = getUserAndIdToken().first?.uid ?: ""
        val chatRoomsDB = Firebase.database(BuildConfig.FIREBASE_BASE_URL)
            .reference.child(CHAT_ROOMS)
            .child(userId)

        return try {
            val chatRoomsSnapshot = chatRoomsDB.get().await()
            val chatRoomItemList = mutableListOf<ChatRoomItem>()

            for (childSnapshot in chatRoomsSnapshot.children) {
                val chatRoomItem = childSnapshot.getValue(ChatRoomItem::class.java)
                if (chatRoomItem != null) {
                    chatRoomItemList.add(chatRoomItem)
                }
            }

            ApiResultSuccess(chatRoomItemList)
        } catch (e: Exception) {
            ApiResultError(code = 400, message = e.message ?: "Failed to get chat rooms")
        }
    }

    override fun addChatDetailEventListener(
        chatRoomId: String,
        onChatItemAdded: (ChatItem) -> Unit
    ): ChildEventListener {
        val chatDetailEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatItem = snapshot.getValue(ChatItem::class.java)
                chatItem?.let { onChatItemAdded(it) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        database.child(CHATS).child(chatRoomId)
            .addChildEventListener(chatDetailEventListener)

        return chatDetailEventListener
    }

    override fun removeChatDetailEventListener(
        chatDetailEventListener: ChildEventListener?,
        chatRoomId: String
    ) {
        chatDetailEventListener?.let {
            chatRef.child(chatRoomId).removeEventListener(it)
        }
    }

    override suspend fun addChatRoomsValueEventListener(callback: (List<ChatRoomItem>) -> Unit): ValueEventListener {
        val userId = getUserAndIdToken().first?.uid ?: ""
        val chatRoomsValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomList = snapshot.children.mapNotNull {
                    it.getValue(ChatRoomItem::class.java)
                }
                callback(chatRoomList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
            }
        }

        chatRoomsRef.child(userId).addValueEventListener(chatRoomsValueEventListener)

        return chatRoomsValueEventListener
    }

    override suspend fun removeChatRoomsValueEventListener(valueEventListener: ValueEventListener?) {
        val userId = getUserAndIdToken().first?.uid ?: ""
        valueEventListener?.let {
            chatRoomsRef.child(userId).removeEventListener(it)
        }
    }

    override suspend fun getDownloadUrl(imageLocation: String): String {
        return storage.getReference(imageLocation)
            .downloadUrl
            .await()
            .toString()
    }

    override suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?> {
        val user = FirebaseAuth.getInstance().currentUser
        val idToken = user?.getIdToken(true)?.await()?.token
        return Pair(user, idToken)
    }

    private suspend fun uploadImages(imageContentList: List<ImageContent>): List<String> =
        coroutineScope {
            imageContentList.map { image ->
                uploadImage(image)
            }
        }

    private suspend fun uploadImage(image: ImageContent): String {
        val location = "images/${image.fileName}"
        val imageRef = storage.getReference(location)
        imageRef
            .putFile(image.uri)
            .await()
        return location
    }

    companion object {
        const val TAG = "FirebaseDataSource"
    }
}
