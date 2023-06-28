package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource

class UserInfoRepository(private val marketNetworkDataSource: MarketNetworkDataSource) {

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
}
