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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val apiClient: ApiClient,
    private val storage: FirebaseStorage
) : MarketNetworkDataSource {

    private val chatRoomsRef = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference.child(CHAT_ROOMS)
    private val chatRef = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference.child(CHATS)
    private val database = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference

    override suspend fun currentUserExists(): Boolean {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null) {
            try {
                val response = apiClient.getUser(idToken)
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null) {
                        return users.containsKey(user?.uid)
                    }
                } else {
                    Log.d("currentUserExists Error", "${Exception(response.code().toString())}")
                }
            } catch (e: Exception) {
                Log.d("currentUserExists Error", e.toString())
            }
        }
        return false
    }

    override suspend fun addUser(nickname: String): Boolean {
        val (user, idToken) = getUserAndIdToken()
        val userItem = User(user?.uid, nickname, null)
        if (idToken != null) {
            try {
                val response = apiClient.addUser(user?.uid ?: "", userItem, idToken)
                if (response.isSuccessful) {
                    Log.d("postUser Success", "${response.body()}")
                    return true
                } else {
                    Log.e("FirebaseDataSource", "postUser Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FirebaseDataSource", "postUser Error: $e")
            }
        }
        return false
    }

    override suspend fun checkDuplicateUserName(nickname: String): Boolean {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null) {
            try {
                val response = apiClient.getUser(idToken)
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null) {
                        return users.values.flatMap { it.values }.any { userInfo ->
                            userInfo.userName == nickname
                        }
                    }
                } else {
                    Log.e(
                        "checkDuplicateUserName Error",
                        "${Exception(response.code().toString())}"
                    )
                }
            } catch (e: Exception) {
                Log.e("checkDuplicateUserName Error", e.toString())
            }
        }
        return false
    }

    override suspend fun addProductPost(
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
    ): Boolean = coroutineScope {
        val (user, idToken) = getUserAndIdToken()

        if (idToken != null && user != null) {
            val userUid = user.uid
            val productPostItem = ProductPostItem(
                userUid,
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

            val response = addPostItem(productPostItem, idToken)
            try {
                if (response.isSuccessful) {
                    Log.d(TAG, "글이 성공적으로 작성되었습니다.")
                    val updateResult = updateMyLatLng(latLng)
                    if (updateResult) {
                        return@coroutineScope true
                    } else {
                        Log.e(TAG, "위치 정보 업데이트에 실패하였습니다.")
                    }
                } else {
                    val statusCode = response.code()
                    Log.e(TAG, "글 작성에 실패하였습니다. (Status Code: $statusCode)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "글 작성을 추가 하던 중 예외가 발생하였습니다.", e)
            }
        } else {
            Log.d(TAG, "GoogleIdToken, User가 존재하지 않습니다.")
        }

        return@coroutineScope false
    }

    override suspend fun getMyDataId(): String? {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null && user?.uid != null) {
            try {
                val response = apiClient.getUser(idToken)
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null) {
                        for ((userNode, userDataMap) in users) {
                            for ((key, foundUser) in userDataMap) {
                                if (foundUser.userId == user.uid) {
                                    return key
                                }
                            }
                        }
                    }
                } else {
                    Log.e(
                        TAG,
                        "getUserNode Error: API call failed with response code: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "getUserNode Error: $e")
            }
        }
        return null
    }

    override suspend fun updateMyLatLng(latLng: String): Boolean {
        try {
            val (user, idToken) = getUserAndIdToken()

            if (user?.uid != null && idToken != null) {
                val dataId = getMyDataId()
                val existingUserResponse = apiClient.getUser(idToken)
                val userMap = existingUserResponse.body()

                userMap?.let { users ->
                    val foundUser = users.values.flatMap { it.values }.find { it.userId == user.uid }
                    foundUser?.let {
                        val updatedUser = User(userId = it.userId, userName = it.userName, latLng = latLng)
                        try {
                            if (dataId != null) {
                                val response = apiClient.updateMyLatLng(user.uid, dataId, updatedUser)
                                if (response.isSuccessful) {
                                    Log.d(TAG, "위치 정보 업데이트 성공")
                                    return true
                                } else {
                                    Log.e(TAG, "위치 정보 업데이트 실패 (Status Code: ${response.code()})")
                                }
                            } else {
                                Log.e(TAG, "데이터 ID가 없습니다.")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "위치 정보 업데이트 오류", e)
                        }
                    }
                }
            } else {
                Log.d(TAG, "GoogleIdToken, User가 존재하지 않습니다.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "예외 발생", e)
        }

        return false
    }

    override suspend fun getProduct(): List<Pair<String, ProductPostItem>> {
        val (user, idToken) = getUserAndIdToken()
        return try {
            if (idToken != null) {
                val response = apiClient.getProduct(idToken)
                if (response.isSuccessful) {
                    val productMap: Map<String, ProductPostItem>? = response.body()
                    val productList = productMap?.entries?.map { entry ->
                        val key = entry.key
                        val product = entry.value
                        val updatedImageLocations = product.imageLocations?.mapNotNull { imageLocation ->
                            if (imageLocation != null) {
                                getDownloadUrl(imageLocation)
                            } else {
                                null
                            }
                        }
                        key to product.copy(imageLocations = updatedImageLocations)
                    } ?: emptyList()

                    productList
                } else {
                    val statusCode = response.code()
                    Log.e(TAG, "Failed to get product. Status Code: $statusCode")
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting product", e)
            emptyList()
        }
    }

    override suspend fun getProductDetail(postId: String): ProductPostItem? {
        val (user, idToken) = getUserAndIdToken()
        return try {
            if (idToken != null) {
                val response = apiClient.getProductDetail(postId, idToken)
                if (response.isSuccessful) {
                    val product = response.body()
                    product
                } else {
                    val statusCode = response.code()
                    Log.e(TAG, "Failed to get product detail. Status Code: $statusCode")
                    throw Exception("Failed to get product detail. Status Code: $statusCode")
                }
            } else {
                throw Exception("Missing GoogleIdToken")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting product detail", e)
            throw Exception("Failed to get product detail", e)
        }
    }

    override suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?> {
        val user = FirebaseAuth.getInstance().currentUser
        var idToken: String? = null

        try {
            idToken = user?.getIdToken(true)?.await()?.token
        } catch (e: Exception) {
            Log.e("idToken Error", e.toString())
        }
        return Pair(user, idToken)
    }

    override suspend fun getUserNameByUserId(userIdToken: String): String? {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null) {
            try {
                val response = apiClient.getUser(idToken)
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null) {
                        val matchingUser = users.values.flatMap { it.values }.find { userInfo ->
                            userInfo.userId == userIdToken
                        }
                        return matchingUser?.userName
                    }
                } else {
                    val statusCode = response.code()
                    Log.e(TAG, "getUserNameByUserId Error, Status Code: $statusCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getUserNameByUserId Error", e)
            }
        }
        return null
    }

    override suspend fun updateProduct(postId: String, request: PatchProductRequest): Boolean {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null) {
            try {
                val response = apiClient.updateProduct(postId, request, idToken)
                if (response.isSuccessful) {
                    Log.d(TAG, "상품 업데이트 성공")
                    return true
                } else {
                    Log.e(TAG, "상품 업데이트 실패 (Status Code: ${response.code()})")
                    return false
                }
            } catch (e: Exception) {
                Log.e(TAG, "상품 업데이트 오류", e)
            }
        }
        return false
    }

    override suspend fun updateProductFavorite(postId: String, request: FavoriteCountRequest): Boolean {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null) {
            try {
                val response = apiClient.updateProductFavorite(postId, request, idToken)
                if (response.isSuccessful) {
                    Log.d(TAG, "좋아요 업데이트 성공")
                    return true
                } else {
                    Log.e(TAG, "좋아요 업데이트 (Status Code: ${response.code()})")
                    return false
                }
            } catch (e: Exception) {
                Log.e(TAG, "좋아요 업데이트", e)
            }
        }
        return false
    }

    override suspend fun buyProduct(postId: String, request: PatchBuyRequest) {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null) {
            try {
                val response = apiClient.buyProduct(postId, request, idToken)
                if (response.isSuccessful) {
                    Log.d(TAG, "구매 시 업데이트 실패")
                } else {
                    Log.e(TAG, "구매 시 업데이트 실패 (Status Code: ${response.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "구매 시 업데이트 오류", e)
            }
        }
    }

    override suspend fun enterChatRoom(ohterUserId: String, otherUserName: String, otherLocation: String): String {
        val userId = getUserAndIdToken().first?.uid?: ""

        val chatRoomDB = chatRoomsRef.child(userId).child(ohterUserId)
        val dataSnapshot = chatRoomDB.get().await()

        return if (dataSnapshot.value != null) {
            val chatRoom = dataSnapshot.getValue(ChatRoomItem::class.java)
            chatRoom?.chatRoomId ?: ""
        } else {
            val chatRoomId = UUID.randomUUID().toString()
            val newChatRoom = ChatRoomItem(
                chatRoomId = chatRoomId,
                otherUserId = ohterUserId,
                otherUserName = otherUserName,
                otherLocation = otherLocation,
            )
            chatRoomDB.setValue(newChatRoom).await()
            chatRoomId
        }
    }

    override suspend fun getMyUserItem(callback: (User) -> Unit) {
        val userId = getUserAndIdToken().first?.uid?: ""

        database.child(USER).child(userId).get().addOnSuccessListener { dataSnapshot ->
            for (childSnapshot in dataSnapshot.children) {
                val myUserId = childSnapshot.child(USER_ID).getValue(String::class.java)
                val myUserName = childSnapshot.child(USER_NAME).getValue(String::class.java)
                val myLatLng = childSnapshot.child(LAT_LNG).getValue(String::class.java)

                val myUserItem = User(myUserId, myUserName, myLatLng)
                callback(myUserItem)
            }
        }
    }

    override suspend fun getOtherUserItem(userId: String, callback: (User) -> Unit) {
        database.child(USER).child(userId).get().addOnSuccessListener { dataSnapshot ->
            for (childSnapshot in dataSnapshot.children) {
                val otherUserId = childSnapshot.child(USER_ID).getValue(String::class.java)
                val otherUserName = childSnapshot.child(USER_NAME).getValue(String::class.java)
                val otherLatLng = childSnapshot.child(LAT_LNG).getValue(String::class.java)

                val otherUserItem = User(otherUserId, otherUserName, otherLatLng)
                callback(otherUserItem)
            }
        }
    }

    override suspend fun sendMessage(chatRoomId: String, otherUserId: String, message: String, myUserName: String, myLocation: String, lastSentTime: String, myLatLng: String) {
        val userId = getUserAndIdToken().first?.uid?: ""

        val newChatItem = ChatItem(
            message = message,
            userId = userId,
            lastSentTime = lastSentTime
        )

        newChatItem.chatId = database.child(CHATS).child(chatRoomId).push().key
        database.child(CHATS).child(chatRoomId).child(newChatItem.chatId ?: "").setValue(newChatItem)

        val dataId = getMyDataId()
        val uId = getUserAndIdToken().first?.uid?: ""

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
    }

    override suspend fun getChatRooms(callback: (List<ChatRoomItem>) -> Unit) {
        val userId = getUserAndIdToken().first?.uid?: ""
        val chatRoomsDB = Firebase.database(BuildConfig.FIREBASE_BASE_URL).reference.child(CHAT_ROOMS).child(userId)

        val chatRoomsValueEventListener = addChatRoomsValueEventListener(callback)
        chatRoomsDB.addValueEventListener(chatRoomsValueEventListener)
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

    override fun removeChatDetailEventListener(chatDetailEventListener: ChildEventListener?, chatRoomId: String) {
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
        val userId = getUserAndIdToken().first?.uid?: ""
        valueEventListener?.let {
            chatRoomsRef.child(userId).removeEventListener(it)
        }
    }

    suspend fun getDownloadUrl(imageLocation: String): String {
        return storage.getReference(imageLocation)
            .downloadUrl
            .await()
            .toString()
    }

    private suspend fun addPostItem(
        ProductPostItem: ProductPostItem,
        idToken: String
    ): Response<Map<String, String>> {
        return apiClient.addProductPost(ProductPostItem, idToken)
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
