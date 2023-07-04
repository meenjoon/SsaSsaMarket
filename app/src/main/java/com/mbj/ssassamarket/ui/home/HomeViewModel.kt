package com.mbj.ssassamarket.ui.home

import androidx.lifecycle.*
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.data.model.FilterType
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.UserType
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val productRepository: ProductRepository, private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _items = MutableLiveData<Event<List<Pair<String, ProductPostItem>>>>()
    val items: LiveData<Event<List<Pair<String, ProductPostItem>>>>
        get() = _items

    private val _filterType = MutableLiveData<FilterType>()
    val filterType: LiveData<FilterType>
        get() = _filterType

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category>
        get() = _category

    private val _itemRefreshCompleted = MutableLiveData<Event<Boolean>>()
    val itemRefreshCompleted: LiveData<Event<Boolean>> get() = _itemRefreshCompleted

    private val _itemRefreshSuccess = MutableLiveData<Event<Boolean>>()
    val itemRefreshSuccess: LiveData<Event<Boolean>> get() = _itemRefreshSuccess

    private val _itemRefreshResponse = MutableLiveData<Event<Boolean>>()
    val itemRefreshResponse: LiveData<Event<Boolean>> get() = _itemRefreshResponse

    val searchText = MutableLiveData<String>()

    private val productList = MediatorLiveData<List<Pair<String, ProductPostItem>>>()

    init {
        loadAllProducts()
    }

    private fun loadAllProducts() {
        viewModelScope.launch {
            val products = productRepository.getProduct()
            productList.value = products
            setupProductList()
        }
    }

    private fun setupProductList() {
        val currentCategory = category.value
        val currentFilterType = filterType.value

        productList.addSource(category) { category ->
            currentCategory?.let {
                applyFilters()
            }
        }

        productList.addSource(filterType) { filterType ->
            currentFilterType?.let {
                applyFilters()
            }
        }

        productList.addSource(searchText) {
            applyFilters()
        }

        applyFilters()
    }

    fun updateFilterType(filterType: FilterType) {
        _filterType.value = filterType
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
        val currentFilterType = filterType.value ?: FilterType.LATEST
        val currentCategory = category.value
        val currentSearchText = searchText.value

        if (currentCategory == null) {
            _items.value = Event(emptyList())
            return
        }

        val filteredProducts = productList.value.orEmpty().filter { (_, product) ->
            product.category == currentCategory.label &&
                    (currentSearchText.isNullOrBlank() || product.title.contains(
                        currentSearchText,
                        ignoreCase = true
                    ))
        }.toMutableList()

        when (currentFilterType) {
            FilterType.LATEST -> filteredProducts.sortByDescending { (_, product) -> product.createdDate }
            FilterType.PRICE -> filteredProducts.sortBy { (_, product) -> product.price }
            FilterType.FAVORITE -> filteredProducts.sortByDescending { (_, product) -> product.favoriteCount }
        }

        _items.value = Event(filteredProducts)
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
            _itemRefreshCompleted.value = Event(false)
            val products = productRepository.getProduct()
            if (products != null) {
                _itemRefreshResponse.value = Event(true)
                productList.value = products
                applyFilters()
            } else {
                _itemRefreshResponse.value = Event(false)
            }
        }
    }

    fun handleUpdateResponse(response: Boolean) {
        if (response) {
            _itemRefreshSuccess.value = Event(true)
        } else {
            _itemRefreshSuccess.value = Event(true)
        }
        _itemRefreshCompleted.value = Event(true)
    }
}
