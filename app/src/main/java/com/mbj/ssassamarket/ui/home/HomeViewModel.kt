package com.mbj.ssassamarket.ui.home

import androidx.lifecycle.*
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.data.model.ProductFilterType
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.UserType
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.local.entities.ProductEntity
import com.mbj.ssassamarket.data.source.local.entities.toProductPostItem
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val userInfoRepository: UserInfoRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<Pair<String, ProductPostItem>>>(emptyList())
    val items: StateFlow<List<Pair<String, ProductPostItem>>> = _items

    private val _productFilterType = MutableStateFlow<ProductFilterType>(ProductFilterType.LATEST)
    val productFilterType: StateFlow<ProductFilterType> = _productFilterType

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    val searchText = MutableStateFlow("")

    private val productList = MutableStateFlow<List<Pair<String, ProductPostItem>>>(emptyList())

    fun loadAllProducts() {
        _isLoading.value = true
        _isError.value = false
        viewModelScope.launch {
            productRepository.getProduct(
                onComplete = { },
                onError =
                {
                    _isError.value = true
                    _isLoading.value = false
                    updateProductsFromRoomDatabase()
                }
            ).collectLatest { productMap ->
                if (productMap is ApiResultSuccess) {
                    val updatedProducts = filterAndMapProducts(productMap.data)
                    val productEntities = convertToProductEntities(updatedProducts).filter { it.soldOut.not() }
                    productRepository.deleteAllProducts()
                    productRepository.insertProducts(productEntities)
                    productList.value = updatedProducts
                    _isLoading.value = false
                    setupProductList()
                }
            }
        }
    }

    private fun List<ProductEntity>.toTransformedList(): List<Pair<String, ProductPostItem>> {
        return this.flatMap { product ->
            listOf(Pair(product.id, product.toProductPostItem()))
        }
    }

    private fun updateProductsFromRoomDatabase() {
        viewModelScope.launch {
            productRepository.getAllProducts().collectLatest { newProductList ->
                val updatedProductList = newProductList.toTransformedList()
                productList.value = updatedProductList
                setupProductList()
            }
        }
    }

    private suspend fun setupProductList() {
        val currentCategory = category.value
        val currentFilterType = productFilterType.value

        productList.collectLatest { products ->
            val filteredProducts = products.filter { (_, product) ->
                product.category == currentCategory?.label &&
                        (searchText.value.isBlank() || product.title.contains(
                            searchText.value,
                            ignoreCase = true
                        ))
            }.toMutableList()

            when (currentFilterType) {
                ProductFilterType.LATEST -> filteredProducts.sortByDescending { (_, product) -> product.createdDate }
                ProductFilterType.PRICE -> filteredProducts.sortBy { (_, product) -> product.price }
                ProductFilterType.FAVORITE -> filteredProducts.sortByDescending { (_, product) -> product.favoriteCount }
            }

            _items.value = filteredProducts
        }
    }

    fun updateFilterType(productFilterType: ProductFilterType) {
        _productFilterType.value = productFilterType
        applyFilters()
    }

    fun updateCategory(category: Category) {
        _category.value = category
        applyFilters()
    }

    fun updateSearchText() {
        applyFilters()
    }

    private fun applyFilters() {
        val currentFilterType = productFilterType.value
        val currentCategory = category.value
        val currentSearchText = searchText.value

        if (currentCategory == null) {
            _items.value = emptyList()
            return
        }

        val filteredProducts = productList.value.filter { (_, product) ->
            product.category == currentCategory.label &&
                    (currentSearchText.isBlank() || product.title.contains(
                        currentSearchText,
                        ignoreCase = true
                    ))
        }.toMutableList()

        when (currentFilterType) {
            ProductFilterType.LATEST -> filteredProducts.sortByDescending { (_, product) -> product.createdDate }
            ProductFilterType.PRICE -> filteredProducts.sortBy { (_, product) -> product.price }
            ProductFilterType.FAVORITE -> filteredProducts.sortByDescending { (_, product) -> product.favoriteCount }
        }

        _items.value = filteredProducts
    }

    fun navigateBasedOnUserType(productIdToken: String, callback: (UserType) -> Unit) {
        viewModelScope.launch {
            val response = userInfoRepository.getUserAndIdToken()
            val idToken = response.first?.uid
            val userType = if (productIdToken == idToken) UserType.SELLER else UserType.BUYER

            callback(userType)
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            productRepository.getProduct(
                onComplete = { },
                onError =
                {
                    _isError.value = true
                    _isLoading.value = false
                    updateProductsFromRoomDatabase()
                }
            ).collectLatest { productMap ->
                if (productMap is ApiResultSuccess) {
                    val updatedProducts = filterAndMapProducts(productMap.data)
                    val productEntities = convertToProductEntities(updatedProducts).filter { it.soldOut.not() }
                    productRepository.deleteAllProducts()
                    productRepository.insertProducts(productEntities)
                    productList.value = updatedProducts
                    _isLoading.value = false
                    applyFilters()
                }
            }
        }
    }

    private suspend fun filterAndMapProducts(productMap: Map<String, ProductPostItem>): List<Pair<String, ProductPostItem>> {
        return productMap.filter { (_, product) -> product.soldOut.not() }
            .mapNotNull { (key, product) ->
                val updatedImageLocations = product.imageLocations?.mapNotNull { imageLocation ->
                    imageLocation?.let { productRepository.getDownloadUrl(it) }
                }
                if (updatedImageLocations != null) {
                    key to product.copy(imageLocations = updatedImageLocations)
                } else {
                    null
                }
            }
    }

    private fun convertToProductEntities(productMap: List<Pair<String, ProductPostItem>>): List<ProductEntity> {
        return productMap.map { pair ->
            val postId = pair.first
            val productPostItem = pair.second
            ProductEntity(
                id = postId,
                uid = productPostItem.id,
                content = productPostItem.content,
                createdDate = productPostItem.createdDate,
                imageLocations = productPostItem.imageLocations,
                price = productPostItem.price,
                title = productPostItem.title,
                category = productPostItem.category,
                soldOut = productPostItem.soldOut,
                favoriteCount = productPostItem.favoriteCount,
                shoppingList = productPostItem.shoppingList ?: emptyList(),
                location = productPostItem.location,
                latLng = productPostItem.latLng,
                favoriteList = productPostItem.favoriteList ?: emptyList()
            )
        }
    }
}
