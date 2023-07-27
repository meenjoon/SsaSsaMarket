package com.mbj.ssassamarket.di

import com.mbj.ssassamarket.BuildConfig
import com.mbj.ssassamarket.data.source.remote.FcmDataSource
import com.mbj.ssassamarket.data.source.remote.MarketNotificationDataSource
import com.mbj.ssassamarket.data.source.remote.network.ApiCallAdapterFactory
import com.mbj.ssassamarket.data.source.remote.network.FcmClient
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FcmClientModule {

    @Singleton
    @Provides
    @Named("FcmRetrofit")
    fun provideFcmRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FCM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ApiCallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideFcmClient(@Named("FcmRetrofit") fcmRetrofit: Retrofit): FcmClient {
        return fcmRetrofit.create(FcmClient::class.java)
    }

    @Singleton
    @Provides
    fun provideMarketNotificationDataSource(
        fcmClient: FcmClient,
    ): MarketNotificationDataSource {
        return FcmDataSource(fcmClient)
    }
}
