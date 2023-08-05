package com.mbj.ssassamarket.data.source.local

import com.mbj.ssassamarket.data.source.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

interface MarketDatabaseDataSource {

    suspend fun insertProducts(products: List<ProductEntity>)

    suspend fun deleteAllProducts()

    fun getAllProducts(): Flow<List<ProductEntity>>
}
