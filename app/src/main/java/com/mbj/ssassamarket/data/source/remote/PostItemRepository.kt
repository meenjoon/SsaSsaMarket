package com.mbj.ssassamarket.data.source.remote

import android.util.Log
import com.mbj.ssassamarket.data.model.ImageContent
import javax.inject.Inject

class PostItemRepository @Inject constructor(private val marketNetworkDataSource: MarketNetworkDataSource) {

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

    companion object {
        private val TAG = "WritingRepository"
    }
}
