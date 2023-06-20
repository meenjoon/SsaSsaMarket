package com.mbj.ssassamarket.data.source.remote

import com.mbj.ssassamarket.BuildConfig
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

interface ApiClient {

    companion object {
        private const val FIREBASE_BASE_URL = BuildConfig.FIREBASE_BASE_URL

        fun create(): ApiClient {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            val moshi: Moshi = Moshi.Builder()
                .build()

            return Retrofit.Builder()
                .baseUrl(FIREBASE_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ApiClient::class.java)
        }
    }
}

