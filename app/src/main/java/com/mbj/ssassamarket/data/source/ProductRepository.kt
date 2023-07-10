package com.mbj.ssassamarket.data.source

import android.util.Log
import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.remote.MarketNetworkDataSource
import javax.inject.Inject

class ProductRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

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
    ): Boolean {
        return try {
            marketNetworkDataSource.addProductPost(
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
        } catch (e: Exception) {
            Log.e(TAG, "포스트 추가 중 예외가 발생하였습니다.", e)
            return false
        }
    }

    suspend fun getProduct(): List<Pair<String, ProductPostItem>> {
        return try {
            marketNetworkDataSource.getProduct()
        } catch (e: Exception) {
            Log.e(TAG, "Product 가져 오던 중 에외가 발생하였습니다.", e)
            emptyList()
        }
    }

    suspend fun getProductDetail(postId: String): ProductPostItem? {
        return try {
            marketNetworkDataSource.getProductDetail(postId)
        } catch (e: Exception) {
            Log.e(TAG, "상세 Product 가져 오던 중 에외가 발생하였습니다.", e)
            throw Exception("상세 Product 가져 오던 중 에외가 발생하였습니다.  $e")
        }
    }

    suspend fun getAvailableProducts() : List<Pair<String, ProductPostItem>> {
        return try {
            marketNetworkDataSource.getProduct().filter { (_, product) -> product.soldOut.not() }
        } catch (e: Exception) {
            Log.e(TAG, "Product 가져 오던 중 에외가 발생하였습니다.", e)
            emptyList()
        }
    }

    suspend fun updateProduct(postId: String, request: PatchProductRequest): Boolean {
        return try {
            marketNetworkDataSource.updateProduct(postId, request)
        } catch (e: Exception) {
            Log.e(TAG, "상품 업데이트 오류", e)
            false
        }
    }

    suspend fun updateProductFavorite(postId: String, request: FavoriteCountRequest): Boolean {
        return try {
            marketNetworkDataSource.updateProductFavorite(postId, request)
        } catch (e: Exception) {
            Log.e(TAG, "좋아요 업데이트 오류", e)
            false
        }
    }

    suspend fun buyProduct(postId: String, request: PatchBuyRequest): Boolean {
        return try {
            marketNetworkDataSource.buyProduct(postId, request)
            true
        } catch (e: Exception) {
            Log.e(TAG, "구매 시 업데이트 오류", e)
            false
        }
    }

    companion object {
        private val TAG = "WritingRepository"
    }
}
