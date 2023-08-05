package com.mbj.ssassamarket.data.source.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mbj.ssassamarket.data.model.ProductPostItem

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "product_id")
    val id: String,
    @ColumnInfo(name = "user_id")
    val uid: String,
    @ColumnInfo(name = "product_content")
    val content: String,
    @ColumnInfo(name = "product_created_date")
    val createdDate: String,
    @ColumnInfo(name = "product_image_locations")
    val imageLocations: List<String?>?,
    @ColumnInfo(name = "product_price")
    val price: Long,
    @ColumnInfo(name = "product_title")
    val title: String,
    @ColumnInfo(name = "product_category")
    val category: String,
    @ColumnInfo(name = "product_sold_out")
    val soldOut: Boolean,
    @ColumnInfo(name = "product_favorite_count")
    val favoriteCount: Int,
    @ColumnInfo(name = "product_shopping_list")
    val shoppingList: List<String?>?,
    @ColumnInfo(name = "product_location")
    val location: String,
    @ColumnInfo(name = "product_lat_lng")
    val latLng: String,
    @ColumnInfo(name = "product_favorite_list")
    val favoriteList: List<String?>?
)

fun ProductEntity.toProductPostItem(): ProductPostItem {
    return ProductPostItem(
        id = this.uid,
        content = this.content,
        createdDate = this.createdDate,
        imageLocations = this.imageLocations,
        price = this.price,
        title = this.title,
        category = this.category,
        soldOut = this.soldOut,
        favoriteCount = this.favoriteCount,
        shoppingList = this.shoppingList,
        location = this.location,
        latLng = this.latLng,
        favoriteList = this.favoriteList
    )
}
