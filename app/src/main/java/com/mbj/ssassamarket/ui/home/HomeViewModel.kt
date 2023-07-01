package com.mbj.ssassamarket.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val filterType: LiveData<FilterType>
        get() = _filterType

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category>
        get() = _category

    val searchText = MutableLiveData<String>()

    fun updateFilterType(filterType: FilterType) {
        _filterType.value = filterType
        loadProductByCategory()
    }

    fun updateCategory(category: Category) {
        _category.value = category
        loadProductByCategory()
    }

    private fun loadProductByCategory() {
        val currentFilterType = filterType.value
        val currentCategory = category.value
        if (currentFilterType != null && currentCategory != null) {
            viewModelScope.launch {
                val productList = productRepository.filterProductsByCategory(
                    currentCategory,
                    currentFilterType
                )
                _items.value = Event(productList)
            }
        }
    }
}
