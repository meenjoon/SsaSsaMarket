package com.mbj.ssassamarket.data.source.remote

interface MarketNetworkDataSource {
    suspend fun currentUserExists(): Boolean
}
