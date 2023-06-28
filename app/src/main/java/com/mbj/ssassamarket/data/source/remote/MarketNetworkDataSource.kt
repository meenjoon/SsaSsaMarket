package com.mbj.ssassamarket.data.source.remote

import com.mbj.ssassamarket.data.model.ImageContent

interface MarketNetworkDataSource {
    suspend fun currentUserExists(): Boolean
    suspend fun addUser(nickname: String): Boolean
    suspend fun checkDuplicateUserName(nickname: String): Boolean
    suspend fun addProductPost(
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
    ): Boolean
    suspend fun getMyDataId(): String?
}
