package com.mbj.ssassamarket.data.source

import android.util.Log
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.data.model.FilterType
import com.mbj.ssassamarket.data.model.ImageContent
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

    suspend fun getProduct(): List<ProductPostItem> {
        return try {
            marketNetworkDataSource.getProduct()
        } catch (e: Exception) {
            Log.e(TAG, "Product 가져 오던 중 에외가 발생하였습니다.", e)
            emptyList()
        }
    }

    suspend fun getProductByCategory(category: Category): List<ProductPostItem> {
        return try {
            val allProducts = getProduct()
            allProducts.filter { it.category == category.label }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting product by category", e)
            emptyList()
        }
    }

    suspend fun filterProductsByCategory(category: Category, filterType: FilterType): List<ProductPostItem> {
        return try {
            val allProducts = getProduct()
            val filteredProducts = allProducts.filter { it.category == category.label }
            when (filterType) {
                FilterType.LATEST -> {
                    filteredProducts.sortedByDescending { it.createdDate }
                }
                FilterType.PRICE -> {
                    filteredProducts.sortedWith(compareBy { it.price })
                }
                FilterType.FAVORITE -> {
                    filteredProducts.sortedWith(compareBy { it.favoriteCount })
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while filtering products by category", e)
            emptyList()
        }
    }

    companion object {
        private val TAG = "WritingRepository"
    }
}
