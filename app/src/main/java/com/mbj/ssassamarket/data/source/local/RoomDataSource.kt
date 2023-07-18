package com.mbj.ssassamarket.data.source.local

import com.mbj.ssassamarket.data.source.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

class RoomDataSource(private val appDatabase: AppDatabase) : MarketDatabaseDataSource {

    override suspend fun insertProducts(products: List<ProductEntity>) {
        appDatabase.productDao().insertProducts(products)
    }

    override fun getAllProducts(): Flow<List<ProductEntity>> {
        return appDatabase.productDao().getAllProducts()
    }
}
