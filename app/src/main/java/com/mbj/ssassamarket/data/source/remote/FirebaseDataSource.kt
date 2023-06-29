package com.mbj.ssassamarket.data.source.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.util.DateFormat.getCurrentTime
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(private val apiClient: ApiClient, private val storage: FirebaseStorage) : MarketNetworkDataSource {

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
                    Log.e("checkDuplicateUserName Error", "${Exception(response.code().toString())}")
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
        price: String,
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
                    Log.e(TAG, "getUserNode Error: API call failed with response code: ${response.code()}")
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

    private suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?> {
        val user = FirebaseAuth.getInstance().currentUser
        var idToken: String? = null

        try {
            idToken = user?.getIdToken(true)?.await()?.token
        } catch (e: Exception) {
            Log.e("idToken Error", e.toString())
        }
        return Pair(user, idToken)
    }

    companion object {
        const val TAG = "FirebaseDataSource"
    }
}
