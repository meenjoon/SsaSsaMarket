package com.mbj.ssassamarket.data.source.remote

import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.User
import retrofit2.Response
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

    @POST("posts/.json")
    suspend fun addProductPost(
        @Body product: ProductPostItem,
        @Query("auth") auth: String
    ): Response<Map<String, String>>

    @PATCH("user/{userId}/{dataId}.json")
    suspend fun updateMyLatLng(
        @Path("userId") userId: String,
        @Path("dataId") dataId: String,
        @Body user: User
    ) : Response<Unit>

    @GET("posts.json")
    suspend fun getProduct(
        @Query("auth") auth: String
    ): Response<Map<String, ProductPostItem>>
}

