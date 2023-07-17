package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

    fun addProductPost(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
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
    ): Flow<ApiResponse<Map<String, String>>> {
        return marketNetworkDataSource.addProductPost(
            onComplete,
            onError,
            content,
            imageLocations,
            price,
            title,
            category,
            soldOut,
            favoriteCount,
            shoppingList,
            location,
            latLng,
            favoriteList
        )
    }

    suspend fun getDownloadUrl(imageLocation: String): String {
        return marketNetworkDataSource.getDownloadUrl(imageLocation)
    }

    fun getProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<ApiResponse<Map<String, ProductPostItem>>> {
        return marketNetworkDataSource.getProduct(onComplete, onError)
    }

    fun updateProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: PatchProductRequest
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.updateProduct(onComplete, onError, postId, request)
    }

    suspend fun buyProduct(postId: String, request: PatchBuyRequest): ApiResponse<Unit> {
        return marketNetworkDataSource.buyProduct(postId, request)
    }

    suspend fun getProductDetail(postId: String): ApiResponse<ProductPostItem> {
        return marketNetworkDataSource.getProductDetail(postId)
    }

    suspend fun updateProductFavorite(
        postId: String,
        request: FavoriteCountRequest
    ): ApiResponse<Unit> {
        return marketNetworkDataSource.updateProductFavorite(postId, request)
    }
}
