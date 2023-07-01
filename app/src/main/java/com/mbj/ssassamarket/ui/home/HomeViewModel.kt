package com.mbj.ssassamarket.ui.home

import androidx.lifecycle.*
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.data.model.FilterType
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val productRepository: ProductRepository) : ViewModel() {

    private val _items = MutableLiveData<Event<List<ProductPostItem>>>()
    val items: LiveData<Event<List<ProductPostItem>>>
        get() = _items

    private val _filterType = MutableLiveData<FilterType>()
    val filterType: LiveData<FilterType>
        get() = _filterType

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category>
        get() = _category

    val searchText = MutableLiveData<String>()

    private val productList = MediatorLiveData<List<ProductPostItem>>()

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

        val filteredProducts = productList.value.orEmpty().filter { product ->
            product.category == currentCategory.label &&
                    (currentSearchText.isNullOrBlank() || product.title.contains(
                        currentSearchText,
                        ignoreCase = true
                    ))
        }.toMutableList()

        when (currentFilterType) {
            FilterType.LATEST -> filteredProducts.sortByDescending { product -> product.createdDate }
            FilterType.PRICE -> filteredProducts.sortBy { product -> product.price }
            FilterType.FAVORITE -> filteredProducts.sortByDescending { product -> product.favoriteCount }
        }

        _items.value = Event(filteredProducts)
    }
}
