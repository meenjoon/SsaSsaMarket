package com.mbj.ssassamarket.data.source

import android.util.Log
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.data.model.PatchProductRequest
import com.mbj.ssassamarket.data.model.ProductPostItem
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

    suspend fun updateProduct(postId: String, request: PatchProductRequest): Boolean {
        return try {
            marketNetworkDataSource.updateProduct(postId, request)
            true
        } catch (e: Exception) {
            Log.e(TAG, "상품 업데이트 오류", e)
            false
        }
    }

    companion object {
        private val TAG = "WritingRepository"
    }
}
