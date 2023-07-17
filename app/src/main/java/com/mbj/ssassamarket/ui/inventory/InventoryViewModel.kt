package com.mbj.ssassamarket.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(private val productRepository: ProductRepository, private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _inventoryDataList = MutableStateFlow<List<InventoryData>>(emptyList())
    val inventoryDataList: StateFlow<List<InventoryData>> = _inventoryDataList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname

    private val _nicknameError = MutableStateFlow(false)
    val nicknameError: StateFlow<Boolean> = _nicknameError

    private val _productError = MutableStateFlow(false)
    val productError: StateFlow<Boolean> = _productError

    private var productPostItemList: List<Pair<String, ProductPostItem>>? = null

    fun initProductPostItemList() {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getProduct(
                onComplete = { _isLoading.value = false },
                onError = { _productError.value = true }
            ).collectLatest { productMap ->
                if (productMap is ApiResultSuccess) {
                    val updateProduct = updateProductsWithImageUrls(productMap.data)
                    productPostItemList = updateProduct
                }
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
            _inventoryDataList.value = inventoryDataList
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
            _inventoryDataList.value = inventoryDataList
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
            _inventoryDataList.value = inventoryDataList
        }
    }

    fun getMyNickname() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            userInfoRepository.getUser(
                onComplete = { _isLoading.value = false },
                onError = { _nicknameError.value = true }
            ).collectLatest { response ->
                if (response is ApiResultSuccess) {
                    val users = response.data
                    val nickname = findNicknameByUserId(users, uId)
                    if (nickname != null) {
                        _nickname.value = nickname
                    }
                }
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
