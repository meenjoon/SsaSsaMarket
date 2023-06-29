package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import javax.inject.Inject

class UserInfoRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

    suspend fun currentUserExists(): Boolean {
        return marketNetworkDataSource.currentUserExists()
    }

    suspend fun addUser(nickname: String): Boolean {
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
}
