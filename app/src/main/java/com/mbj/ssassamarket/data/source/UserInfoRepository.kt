package com.mbj.ssassamarket.data.source

import com.google.firebase.auth.FirebaseUser
import com.mbj.ssassamarket.data.model.PatchUserLatLng
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import javax.inject.Inject

class UserInfoRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

    suspend fun getUser(): ApiResponse<Map<String, Map<String, User>>> {
        return marketNetworkDataSource.getUser()
    }

    suspend fun addUser(nickname: String): ApiResponse<Map<String, String>> {
        return marketNetworkDataSource.addUser(nickname)
    }

    suspend fun updateMyLatLng(dataId: String, latLng: PatchUserLatLng): ApiResponse<Unit> {
        return marketNetworkDataSource.updateMyLatLng(dataId, latLng)
    }

    suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?> {
        return marketNetworkDataSource.getUserAndIdToken()
    }
}
