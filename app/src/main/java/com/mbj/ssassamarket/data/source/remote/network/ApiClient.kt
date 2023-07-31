package com.mbj.ssassamarket.data.source.remote.network

import com.mbj.ssassamarket.data.model.*
import retrofit2.http.*

interface ApiClient {

    @POST("user/{uId}.json")
    suspend fun addUser(
        @Path("uId") uId: String,
        @Body user: User,
        @Query("auth") auth: String
    ): ApiResponse<Map<String, String>>

    @GET("user.json")
    suspend fun getUser(
        @Query("auth") auth: String
    ): ApiResponse<Map<String, Map<String, User>>>

    @POST("posts/.json")
    suspend fun addProductPost(
        @Body product: ProductPostItem,
        @Query("auth") auth: String
    ): ApiResponse<Map<String, String>>

    @PATCH("user/{userId}/{dataId}.json")
    suspend fun updateMyLatLng(
        @Path("userId") userId: String,
        @Path("dataId") dataId: String,
        @Body request: PatchUserLatLng,
        @Query("auth") auth: String
    ): ApiResponse<Unit>

    @GET("posts.json")
    suspend fun getProduct(
        @Query("auth") auth: String
    ): ApiResponse<Map<String, ProductPostItem>>

    @PATCH("posts/{postId}.json")
    suspend fun updateProduct(
        @Path("postId") postId: String,
        @Body request: PatchProductRequest,
        @Query("auth") auth: String
    ): ApiResponse<Unit>

    @PATCH("posts/{postId}.json")
    suspend fun buyProduct(
        @Path("postId") postId: String,
        @Body request: PatchBuyRequest,
        @Query("auth") auth: String
    ): ApiResponse<Unit>

    @PATCH("posts/{postId}.json")
    suspend fun updateProductFavorite(
        @Path("postId") productId: String,
        @Body requestBody: FavoriteCountRequest,
        @Query("auth") auth: String
    ): ApiResponse<Unit>

    @GET("posts/{postId}.json")
    suspend fun getProductDetail(
        @Path("postId") postId: String,
        @Query("auth") auth: String
    ): ApiResponse<ProductPostItem>

    @DELETE("user/{uid}.json")
    suspend fun deleteUserData(
        @Path("uid") uid: String,
        @Query("auth") auth: String
    ): ApiResponse<Unit>

    @DELETE("posts/{postId}.json")
    suspend fun deleteProductData(
        @Path("postId") postId: String,
        @Query("auth") auth: String
    ): ApiResponse<Unit>

    @GET("chatRooms.json")
    suspend fun getAllChatRoomData(
        @Query("auth") auth: String
    ): ApiResponse<Map<String, Map<String, ChatRoomItem>>>
}

