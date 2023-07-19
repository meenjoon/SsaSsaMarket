package com.mbj.ssassamarket.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mbj.ssassamarket.data.source.local.dao.ProductDao
import com.mbj.ssassamarket.data.source.local.entities.ProductEntity

@Database(entities = [ProductEntity::class], version = 2)
@TypeConverters(ProductTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
}
