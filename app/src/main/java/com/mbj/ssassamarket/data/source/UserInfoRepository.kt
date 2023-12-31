package com.mbj.ssassamarket.data.source

import com.google.firebase.auth.FirebaseUser
import com.mbj.ssassamarket.data.model.PatchUserLatLng
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserInfoRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

    fun getUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, Map<String, User>>>> {
        return marketNetworkDataSource.getUser(onComplete, onError)
    }

    fun addUser(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        nickname: String
    ): Flow<ApiResponse<Map<String, String>>> {
        return marketNetworkDataSource.addUser(onComplete, onError, nickname)
    }

    fun updateMyLatLng(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        dataId: String,
        latLng: PatchUserLatLng
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.updateMyLatLng(onComplete, onError, dataId, latLng)
    }

    fun logout(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.logout(onComplete, onError)
    }

    fun deleteUserData(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        uid: String
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.deleteUserData(onComplete, onError, uid)
    }

    fun updateUserFcmToken(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        userId: String,
        userPostKey: String
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.updateUserFcmToken(onComplete, onError, userId, userPostKey)
    }

    suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?> {
        return marketNetworkDataSource.getUserAndIdToken()
    }
}
