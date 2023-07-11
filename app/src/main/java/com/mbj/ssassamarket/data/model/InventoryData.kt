package com.mbj.ssassamarket.data.model


private const val VIEW_TYPE_INVENTORY = 0
private const val VIEW_TYPE_PRODUCT_LIST = 1

sealed class InventoryData {

    data class ProductType(val inventoryType: InventoryType) : InventoryData() {
        override val id: Int
            get() = VIEW_TYPE_INVENTORY
    }

    data class ProductItem(val productList: List<ProductPostItem>) : InventoryData() {
        override val id: Int
            get() = VIEW_TYPE_PRODUCT_LIST
    }

    abstract val id: Int
}
