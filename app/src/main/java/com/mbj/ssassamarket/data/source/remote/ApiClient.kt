package com.mbj.ssassamarket.data.source.remote

import com.mbj.ssassamarket.BuildConfig
import com.mbj.ssassamarket.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface ApiClient {

    @POST("user/{uId}.json")
    suspend fun addUser(
        @Path("uId") uId: String,
        @Body user: User,
        @Query("auth") auth: String
    ): Response<Map<String, String>>

    @GET("user.json")
    suspend fun getUser(
        @Query("auth") auth: String
    ): Response<Map<String, Map<String, User>>>

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
                .add(KotlinJsonAdapterFactory())
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

