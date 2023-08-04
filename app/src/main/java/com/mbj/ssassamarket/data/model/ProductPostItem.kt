package com.mbj.ssassamarket.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductPostItem(
    @Json(name = "id")
    val id: String,
    @Json(name = "content")
    val content: String,
    @Json(name = "createdDate")
    val createdDate: String,
    @Json(name = "imageLocations")
    val imageLocations: List<String?>? = null,
    @Json(name = "price")
    val price: Long,
    @Json(name = "title")
    val title: String,
    @Json(name = "category")
    val category: String,
    @Json(name = "soldOut")
    val soldOut: Boolean,
    @Json(name = "favoriteCount")
    val favoriteCount: Int,
    @Json(name = "shoppingList")
    val shoppingList: List<String?>? = null,
    @Json(name = "location")
    val location: String,
    @Json(name = "latLng")
    val latLng: String,
    @Json(name = "favoriteList")
    val favoriteList: List<String?>? = null
) : java.io.Serializable

data class PatchProductRequest(
    val title: String,
    val price: Long,
    val content: String
)

data class PatchBuyRequest(
    val soldOut: Boolean,
    val shoppingList: List<String?>?
)

data class FavoriteCountRequest(
    val favoriteCount: Int,
    val favoriteList: List<String?>?
)
