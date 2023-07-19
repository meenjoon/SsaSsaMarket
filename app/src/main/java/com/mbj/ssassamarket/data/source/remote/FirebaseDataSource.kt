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

    private val chatRoomsRef = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference.child(CHAT_ROOMS)
    private val chatRef = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference.child(CHATS)
    private val database = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    override fun getUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, Map<String, User>>>> = flow {
        try {
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
        } catch (e: Exception) {
            onError(e.message)
        }
    }.onCompletion {
        onComplete()
    }.flowOn(defaultDispatcher)

    override fun addUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        nickname: String
    ): Flow<ApiResponse<Map<String, String>>> = flow<ApiResponse<Map<String, String>>> {
        try {
            val (user, idToken) = getUserAndIdToken()
            val googleIdToken = idToken ?: ""
            val userItem = User(user?.uid, nickname, null)
            val response = apiClient.addUser(user?.uid ?: "", userItem, googleIdToken)

            response.onSuccess { data ->
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
        try {
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

    override fun updateMyLatLng(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        dataId: String,
        latLng: PatchUserLatLng
    ): Flow<ApiResponse<Unit>> = flow<ApiResponse<Unit>> {
        try {
            val (user, idToken) = getUserAndIdToken()
            val googleIdToken = idToken ?: ""
            val userId = user?.uid ?: ""
            val response = apiClient.updateMyLatLng(userId, dataId, latLng, googleIdToken)

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

    override fun getProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, ProductPostItem>>> = flow<ApiResponse<Map<String, ProductPostItem>>> {
        try {
            val (user, idToken) = getUserAndIdToken()
            val googleIdToken = idToken ?: ""
            val response = apiClient.getProduct(googleIdToken)

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

    override fun updateProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: PatchProductRequest
    ): Flow<ApiResponse<Unit>> = flow<ApiResponse<Unit>> {
        try {
            val (user, idToken) = getUserAndIdToken()
            val googleIdToken = idToken ?: ""
            val response = apiClient.updateProduct(postId, request, googleIdToken)

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

    override fun buyProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: PatchBuyRequest,
    ): Flow<ApiResponse<Unit>> = flow<ApiResponse<Unit>> {
        try {
            val (user, idToken) = getUserAndIdToken()
            val googleIdToken = idToken ?: ""
            val response = apiClient.buyProduct(postId, request, googleIdToken)

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

    override fun enterChatRoom(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        ohterUserId: String,
        otherUserName: String,
        otherLocation: String
    ): Flow<ApiResponse<String>> = flow<ApiResponse<String>> {
        try {
            val userId = getUserAndIdToken().first?.uid ?: ""
            if (userId.isEmpty()) {
                onError("Failed to get the user ID.")
                return@flow
            }

            val chatRoomDB = chatRoomsRef.child(userId).child(ohterUserId)
            val dataSnapshot = chatRoomDB.get().await()

            if (dataSnapshot.value != null) {
                val chatRoom = dataSnapshot.getValue(ChatRoomItem::class.java)
                val chatRoomId = chatRoom?.chatRoomId ?: ""
                emit(ApiResultSuccess(chatRoomId))
            } else {
                val chatRoomId = UUID.randomUUID().toString()
                val newChatRoom = ChatRoomItem(
                    chatRoomId = chatRoomId,
                    otherUserId = ohterUserId,
                    otherUserName = otherUserName,
                    otherLocation = otherLocation,
                )
                chatRoomDB.setValue(newChatRoom).await()
                emit(ApiResultSuccess(chatRoomId))
            }
        } catch (e: Exception) {
            onError(e.message)
        }
    }.onCompletion {
        onComplete()
    }.flowOn(defaultDispatcher)

    override fun getProductDetail(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String
    ): Flow<ApiResponse<ProductPostItem>> = flow<ApiResponse<ProductPostItem>> {
        try {
            val (user, idToken) = getUserAndIdToken()
            val googleIdToken = idToken ?: ""
            val response = apiClient.getProductDetail(postId, googleIdToken)

            response.onSuccess { productItem ->
                emit(ApiResultSuccess(productItem))
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

    override fun updateProductFavorite(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: FavoriteCountRequest
    ): Flow<ApiResponse<Unit>> = flow<ApiResponse<Unit>> {
        try {
            val (user, idToken) = getUserAndIdToken()
            val googleIdToken = idToken ?: ""
            val response = apiClient.updateProductFavorite(postId, request, googleIdToken)

            response.onSuccess {
                emit(ApiResultSuccess(Unit))
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

    override fun getMyUserItem(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<User>> = flow<ApiResponse<User>> {
        try {
            val userId = getUserAndIdToken().first?.uid ?: ""

            val dataSnapshot = database.child(USER).child(userId).get().await()
            for (childSnapshot in dataSnapshot.children) {
                val myUserId = childSnapshot.child(USER_ID).getValue(String::class.java)
                val myUserName = childSnapshot.child(USER_NAME).getValue(String::class.java)
                val myLatLng = childSnapshot.child(LAT_LNG).getValue(String::class.java)

                val myUserItem = User(myUserId, myUserName, myLatLng)
                emit(ApiResultSuccess(myUserItem))
                return@flow
            }
            emit(ApiResultError(code = 400, message = "My User data not found"))
        } catch (e: Exception) {
            onError(e.message)
            emit(ApiResultError(code = 500, message = e.message ?: "Failed to get my user"))
        }
    }.onCompletion {
        onComplete()
    }.flowOn(defaultDispatcher)

    override fun getOtherUserItem(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        userId: String
    ): Flow<ApiResponse<User>> = flow<ApiResponse<User>> {
        try {
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
                emit(ApiResultSuccess(user))
            } else {
                onError("Other User data not found")
                emit(ApiResultError(code = 400, message = "Other User data not found"))
            }
        } catch (e: Exception) {
            onError(e.message)
            emit(ApiResultError(code = 500, message = e.message ?: "Failed to get other user"))
        }
    }.onCompletion {
        onComplete()
    }.flowOn(defaultDispatcher)

    override fun sendMessage(
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
    ): Flow<ApiResponse<Unit>> = flow<ApiResponse<Unit>> {
        try {
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

            emit(ApiResultSuccess(Unit))
        } catch (e: Exception) {
            onError(e.message)
            emit(ApiResultError(code = 400, message = e.message ?: "Failed to send message"))
        }
    }.onCompletion {
        onComplete()
    }.flowOn(defaultDispatcher)

    override fun getChatRooms(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<List<ChatRoomItem>>> = flow<ApiResponse<List<ChatRoomItem>>> {
        try {
            val userId = getUserAndIdToken().first?.uid ?: ""
            val chatRoomsDB = Firebase.database(BuildConfig.FIREBASE_BASE_URL)
                .reference.child(CHAT_ROOMS)
                .child(userId)

            val chatRoomsSnapshot = chatRoomsDB.get().await()
            val chatRoomItemList = mutableListOf<ChatRoomItem>()

            for (childSnapshot in chatRoomsSnapshot.children) {
                val chatRoomItem = childSnapshot.getValue(ChatRoomItem::class.java)
                chatRoomItem?.let {
                    chatRoomItemList.add(it)
                }
            }
            emit(ApiResultSuccess<List<ChatRoomItem>>(chatRoomItemList))
        } catch (e: Exception) {
            val errorResponse = ApiResultError<List<ChatRoomItem>>(
                code = 400,
                message = e.message ?: "Failed to get chat rooms"
            )
            emit(
                ApiResultError<List<ChatRoomItem>>(code = 400, message = e.message ?: "Failed to get chat rooms")
            )
            onError(errorResponse.message)
        }
    }.onCompletion {
        onComplete()
    }.flowOn(defaultDispatcher)

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
        try {
            val user = FirebaseAuth.getInstance().currentUser
            val idToken = user?.getIdToken(true)?.await()?.token
            return Pair(user, idToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user and ID token: ${e.message}")
            return Pair(null, null) // 또는 원하는 값으로 반환하거나, 예외를 다시 던질 수도 있습니다.
        }
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
