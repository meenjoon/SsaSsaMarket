package com.mbj.ssassamarket.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val userInfoRepository: UserInfoRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname

    private val _nicknameError = MutableStateFlow(false)
    val nicknameError: StateFlow<Boolean> = _nicknameError

    private val _productError = MutableStateFlow(false)
    val productError: StateFlow<Boolean> = _productError

    val productPostItemList: StateFlow<List<Pair<String, ProductPostItem>>> = initProductPostItemList().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500),
            initialValue = emptyList()
        )

    fun initProductPostItemList(): Flow<List<Pair<String, ProductPostItem>>> {
        _isLoading.value = true

        return productRepository.getProduct(
            onComplete = { },
            onError = {
                _isLoading.value = false
                _productError.value = true
            }
        ).mapNotNull { productMap ->
            if (productMap is ApiResultSuccess) {
                val updatedProducts = updateProductsWithImageUrls(productMap.data)
                updatedProducts
            } else {
                null
            }
        }
    }

    suspend fun getMyFavoriteProduct(productPostItems: List<Pair<String, ProductPostItem>>): List<Pair<String, ProductPostItem>> {
        val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
        return productPostItems.filter { (_, product) ->
            product.favoriteList?.contains(uId) == true
        }.sortedByDescending { it.second.createdDate }
    }

    suspend fun getMyRegisteredProduct(productPostItems: List<Pair<String, ProductPostItem>>): List<Pair<String, ProductPostItem>> {
        val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
        return productPostItems.filter { (_, product) ->
            product.id == uId
        }.sortedByDescending { it.second.createdDate }
    }

    suspend fun getMyPurchasedProduct(productPostItems: List<Pair<String, ProductPostItem>>): List<Pair<String, ProductPostItem>> {
        val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
        return productPostItems.filter { (_, product) ->
            product.shoppingList?.contains(uId) == true
        }.sortedByDescending { it.second.createdDate }
    }

    fun createInventoryDataList(
        myFavoriteProducts: List<Pair<String, ProductPostItem>>,
        myRegisteredProducts: List<Pair<String, ProductPostItem>>,
        myPurchasedProducts: List<Pair<String, ProductPostItem>>
    ): List<InventoryData> {
        val inventoryDataList = mutableListOf<InventoryData>()

        if (myFavoriteProducts.isNotEmpty()) {
            inventoryDataList.add(InventoryData.ProductType(InventoryType.FAVORITE))
            inventoryDataList.add(InventoryData.ProductItem(myFavoriteProducts))
        }

        if (myRegisteredProducts.isNotEmpty()) {
            inventoryDataList.add(InventoryData.ProductType(InventoryType.REGISTER_PRODUCT))
            inventoryDataList.add(InventoryData.ProductItem(myRegisteredProducts))
        }

        if (myPurchasedProducts.isNotEmpty()) {
            inventoryDataList.add(InventoryData.ProductType(InventoryType.SHOPPING_PRODUCT))
            inventoryDataList.add(InventoryData.ProductItem(myPurchasedProducts))
        }
        _isLoading.value = false
        return inventoryDataList
    }

    fun getMyNickname() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            userInfoRepository.getUser(
                onComplete = { },
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
