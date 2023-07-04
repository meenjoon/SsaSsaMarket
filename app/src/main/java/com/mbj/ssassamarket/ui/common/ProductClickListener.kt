package com.mbj.ssassamarket.ui.common

import com.mbj.ssassamarket.data.model.ProductPostItem

interface ProductClickListener {

    fun onProductClick(productPostItem: Pair<String,ProductPostItem>)
}
