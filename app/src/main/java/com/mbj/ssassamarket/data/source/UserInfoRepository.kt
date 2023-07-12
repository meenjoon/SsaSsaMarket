package com.mbj.ssassamarket.data.source

import com.google.firebase.auth.FirebaseUser
import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import javax.inject.Inject

class UserInfoRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

    suspend fun currentUserExists(): Boolean {
        return marketNetworkDataSource.currentUserExists()
    }

    suspend fun addUser(nickname: String): ApiResponse<Map<String, String>> {
        return marketNetworkDataSource.addUser(nickname)
    }

    suspend fun checkDuplicateUserName(nickname: String): Boolean {
        return marketNetworkDataSource.checkDuplicateUserName(nickname)
    }

    suspend fun getMyDataId(): String? {
        return marketNetworkDataSource.getMyDataId()
    }

    suspend fun updateMyLatLng(latLng: String): Boolean {
        return marketNetworkDataSource.updateMyLatLng(latLng)
    }

    suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?> {
        return marketNetworkDataSource.getUserAndIdToken()
    }

    suspend fun getUserNameByUserId(uId: String) : String? {
        return marketNetworkDataSource.getUserNameByUserId(uId)
    }
}
