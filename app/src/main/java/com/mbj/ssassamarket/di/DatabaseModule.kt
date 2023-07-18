package com.mbj.ssassamarket.di

import android.content.Context
import androidx.room.Room
import com.mbj.ssassamarket.data.source.local.AppDatabase
import com.mbj.ssassamarket.data.source.local.MarketDatabaseDataSource
import com.mbj.ssassamarket.data.source.local.RoomDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "ssassa_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideMarketDatabaseDataSource(appDatabase: AppDatabase): MarketDatabaseDataSource {
        return RoomDataSource(appDatabase)
    }
}
