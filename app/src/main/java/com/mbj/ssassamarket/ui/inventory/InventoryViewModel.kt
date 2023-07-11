package com.mbj.ssassamarket.ui.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.InventoryData
import com.mbj.ssassamarket.data.model.InventoryType
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(private val productRepository: ProductRepository, private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _inventoryDataList = MutableLiveData<Event<List<InventoryData>>>()
    val inventoryDataList: LiveData<Event<List<InventoryData>>> get() = _inventoryDataList

    private var productPostItemList : List<ProductPostItem>?= null

    init {
        viewModelScope.launch {
            initProductPostItemList()
            getMyFavoriteProduct()
            getMyRegisteredProduct()
            getMyPurchasedProduct()
        }
    }

    private fun getMyFavoriteProduct() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val inventoryDataList = mutableListOf<InventoryData>()
            val favoriteProductList = productPostItemList?.filter { product ->
                product.favoriteList?.contains(uId) == true
            } ?: emptyList()

            if (favoriteProductList.isNotEmpty()) {
                inventoryDataList.add(InventoryData.ProductType(InventoryType.FAVORITE))
                inventoryDataList.add(InventoryData.ProductItem(favoriteProductList))
            }
            _inventoryDataList.value = Event(inventoryDataList)
        }
    }

    private fun getMyRegisteredProduct() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val inventoryDataList = mutableListOf<InventoryData>()
            val registeredProductList = productPostItemList?.filter { product ->
                product.id == uId
            } ?: emptyList()

            if (registeredProductList.isNotEmpty()) {
                inventoryDataList.add(InventoryData.ProductType(InventoryType.REGISTER_PRODUCT))
                inventoryDataList.add(InventoryData.ProductItem(registeredProductList))
            }
            _inventoryDataList.value = Event(inventoryDataList)
        }
    }

    private fun getMyPurchasedProduct() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val inventoryDataList = mutableListOf<InventoryData>()
            val purchasedProductList = productPostItemList?.filter { product ->
                product.shoppingList?.contains(uId) == true
            } ?: emptyList()

            if (purchasedProductList.isNotEmpty()) {
                inventoryDataList.add(InventoryData.ProductType(InventoryType.SHOPPING_PRODUCT))
                inventoryDataList.add(InventoryData.ProductItem(purchasedProductList))
            }
            _inventoryDataList.value = Event(inventoryDataList)
        }
    }

    private suspend fun initProductPostItemList() {
        productPostItemList = productRepository.getProduct().map { it.second }
    }
}
