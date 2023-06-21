package com.mbj.ssassamarket.data.source.remote

interface MarketNetworkDataSource {
    suspend fun currentUserExists(): Boolean
    suspend fun addUser(nickname: String): Boolean
    suspend fun checkDuplicateUserName(nickname: String): Boolean
}
