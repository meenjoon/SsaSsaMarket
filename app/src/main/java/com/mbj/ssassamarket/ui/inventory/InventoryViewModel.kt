package com.mbj.ssassamarket.ui.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.onError
import com.mbj.ssassamarket.data.source.remote.network.onSuccess
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(private val productRepository: ProductRepository, private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _inventoryDataList = MutableLiveData<Event<List<InventoryData>>>()
    val inventoryDataList: LiveData<Event<List<InventoryData>>> get() = _inventoryDataList

    private val _isLoading = MutableLiveData(Event(false))
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _nickname = MutableLiveData<Event<String?>>()
    val nickname: LiveData<Event<String?>> get() = _nickname

    private val _nicknameError = MutableLiveData<Event<Boolean>>()
    val nicknameError: LiveData<Event<Boolean>> get() = _nicknameError

    private val _productError = MutableLiveData<Event<Boolean>>()
    val productError: LiveData<Event<Boolean>> get() = _productError

    private var productPostItemList: List<Pair<String, ProductPostItem>>? = null

    fun initProductPostItemList() {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            val result = productRepository.getProduct()
            result.onSuccess { productMap ->
                val updateProduct = updateProductsWithImageUrls(productMap)
                productPostItemList = updateProduct
                _isLoading.value = Event(false)
            }.onError { code, message ->
                _isLoading.value = Event(false)
                _productError.value = Event(true)
            }
            getMyFavoriteProduct()
            getMyRegisteredProduct()
            getMyPurchasedProduct()
        }
    }

    private fun getMyFavoriteProduct() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val inventoryDataList = mutableListOf<InventoryData>()
            val favoriteProductList = productPostItemList?.filter { (_, product) ->
                product.favoriteList?.contains(uId) == true
            }?.map { (key, product) ->
                key to product
            }?.sortedByDescending { it.second.createdDate } ?: emptyList()

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
            }?.sortedByDescending { it.second.createdDate } ?: emptyList()

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
            }?.sortedByDescending { it.second.createdDate } ?: emptyList()

            if (purchasedProductList.isNotEmpty()) {
                inventoryDataList.add(InventoryData.ProductType(InventoryType.SHOPPING_PRODUCT))
                inventoryDataList.add(InventoryData.ProductItem(purchasedProductList))
            }
            _inventoryDataList.value = Event(inventoryDataList)
        }
    }

    fun getNickname() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val result = userInfoRepository.getUser()
            result.onSuccess { users ->
                val nickname = findNicknameByUserId(users, uId)
                _nickname.value = Event(nickname)
            }.onError { code, message ->
                _nicknameError.value = Event(true)
            }
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

    private fun findNicknameByUserId(
        users: Map<String, Map<String, User>>,
        userId: String
    ): String? {
        val matchingUser = users.values.flatMap { it.values }.find { userInfo ->
            userInfo.userId == userId
        }
        return matchingUser?.userName
    }

    private suspend fun updateProductsWithImageUrls(productMap: Map<String, ProductPostItem>): List<Pair<String, ProductPostItem>> {
        val updatedList = mutableListOf<Pair<String, ProductPostItem>>()
        for ((key, product) in productMap) {
            val updatedImageLocations = product.imageLocations?.mapNotNull { imageLocation ->
                imageLocation?.let { productRepository.getDownloadUrl(it) }
            }
            if (updatedImageLocations != null) {
                val updatedProduct = product.copy(imageLocations = updatedImageLocations)
                updatedList.add(key to updatedProduct)
            }
        }
        return updatedList
    }
}
