package com.mbj.ssassamarket.data.source.local

import com.mbj.ssassamarket.data.source.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

interface MarketDatabaseDataSource {

    suspend fun insertProducts(products: List<ProductEntity>)

    fun getAllProducts(): Flow<List<ProductEntity>>
}
