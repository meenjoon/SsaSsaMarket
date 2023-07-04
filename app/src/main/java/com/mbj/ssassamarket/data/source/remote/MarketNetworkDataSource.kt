package com.mbj.ssassamarket.data.source.remote

import com.google.firebase.auth.FirebaseUser
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.data.model.PatchProductRequest
import com.mbj.ssassamarket.data.model.ProductPostItem

interface MarketNetworkDataSource {
    suspend fun currentUserExists(): Boolean
    suspend fun addUser(nickname: String): Boolean
    suspend fun checkDuplicateUserName(nickname: String): Boolean
    suspend fun addProductPost(
        content: String,
        imageLocations: List<ImageContent>,
        price: Int,
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
    suspend fun updateMyLatLng(latLng: String): Boolean
    suspend fun getProduct(): List<Pair<String, ProductPostItem>>
    suspend fun getUserAndIdToken() : Pair<FirebaseUser?, String?>
    suspend fun getUserNameByUserId(userIdToken: String) : String?
    suspend fun updateProduct(postId: String, request: PatchProductRequest)
}
