package com.mbj.ssassamarket.ui.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.InventoryData
import com.mbj.ssassamarket.data.model.InventoryType
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.UserType
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

    private val _nickname = MutableLiveData<Event<String?>>()
    val nickname: LiveData<Event<String?>> get() = _nickname

    private var productPostItemList : List<Pair<String, ProductPostItem>>?= null

    init {
        viewModelScope.launch {
            getNickname()
            initProductPostItemList()
            getMyFavoriteProduct()
            getMyRegisteredProduct()
            getMyPurchasedProduct()
        }
    }

    private suspend fun initProductPostItemList() {
        productPostItemList = productRepository.getProduct()
    }

    private fun getMyFavoriteProduct() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val inventoryDataList = mutableListOf<InventoryData>()
            val favoriteProductList = productPostItemList?.filter { (_, product) ->
                product.favoriteList?.contains(uId) == true
            }?.map { (key, product) ->
                key to product
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
            val registeredProductList = productPostItemList?.filter { (_, product) ->
                product.id == uId
            }?.map { (key, product) ->
                key to product
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
            val purchasedProductList = productPostItemList?.filter { (_, product) ->
                product.shoppingList?.contains(uId) == true
            }?.map { (key, product) ->
                key to product
            } ?: emptyList()

            if (purchasedProductList.isNotEmpty()) {
                inventoryDataList.add(InventoryData.ProductType(InventoryType.SHOPPING_PRODUCT))
                inventoryDataList.add(InventoryData.ProductItem(purchasedProductList))
            }
            _inventoryDataList.value = Event(inventoryDataList)
        }
    }

    private fun getNickname() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val nickname = userInfoRepository.getUserNameByUserId(uId)
            _nickname.value = Event(nickname)
        }
    }

    fun navigateBasedOnUserType(productIdToken: String, callback: (UserType) -> Unit) {
        viewModelScope.launch {
            val response = userInfoRepository.getUserAndIdToken()
            val idToken = response.first?.uid
            val userType = if (productIdToken == idToken) UserType.SELLER else UserType.BUYER

            callback(userType)
        }
    }
}
