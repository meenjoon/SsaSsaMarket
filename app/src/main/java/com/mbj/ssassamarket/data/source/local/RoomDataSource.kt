package com.mbj.ssassamarket.data.source.local

import com.mbj.ssassamarket.data.source.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomDataSource @Inject constructor(private val appDatabase: AppDatabase) : MarketDatabaseDataSource {

    override suspend fun insertProducts(products: List<ProductEntity>) {
        return appDatabase.productDao().insertProducts(products)
    }

    override suspend fun deleteAllProducts() {
        return appDatabase.productDao().deleteAllProducts()
    }

    override fun getAllProducts(): Flow<List<ProductEntity>> {
        return appDatabase.productDao().getAllProducts()
    }
}
