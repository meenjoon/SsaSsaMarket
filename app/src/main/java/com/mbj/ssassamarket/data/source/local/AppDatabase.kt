package com.mbj.ssassamarket.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mbj.ssassamarket.data.source.local.dao.ProductDao
import com.mbj.ssassamarket.data.source.local.entities.ProductEntity

@Database(entities = [ProductEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java, "ssassa_database"
            ).build()
        }
    }
}
