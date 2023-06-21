package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource

class UserInfoRepository(private val marketNetworkDataSource: MarketNetworkDataSource) {

    suspend fun currentUserExists(): Boolean {
        return marketNetworkDataSource.currentUserExists()
    }
}