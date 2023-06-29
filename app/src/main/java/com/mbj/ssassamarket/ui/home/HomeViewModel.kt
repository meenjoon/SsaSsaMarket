package com.mbj.ssassamarket.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.Category
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

    fun loadProductByCategory(category: Category) {
        viewModelScope.launch {
            val productList = productRepository.getProductByCategory(category)
            _items.value = Event(productList)
        }
    }
}
