package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.local.MarketDatabaseDataSource
import com.mbj.ssassamarket.data.source.local.entities.ProductEntity
import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import com.mbj.ssassamarket.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val marketNetworkDataSource: MarketNetworkDataSource,
    private val marketDatabaseDataSource: MarketDatabaseDataSource
) {

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

    fun buyProduct(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: PatchBuyRequest,
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.buyProduct(onComplete, onError, postId, request)
    }

    fun getProductDetail(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String
    ): Flow<ApiResponse<ProductPostItem>> {
        return marketNetworkDataSource.getProductDetail(onComplete, onError, postId)
    }

    fun updateProductFavorite(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit,
        postId: String,
        request: FavoriteCountRequest
    ): Flow<ApiResponse<Unit>> {
        return marketNetworkDataSource.updateProductFavorite(onComplete, onError, postId, request)
    }

    suspend fun insertProducts(products: List<ProductEntity>) {
        marketDatabaseDataSource.insertProducts(products)
    }

    fun getAllProducts(): Flow<List<ProductEntity>> {
        return marketDatabaseDataSource.getAllProducts()
    }
}
