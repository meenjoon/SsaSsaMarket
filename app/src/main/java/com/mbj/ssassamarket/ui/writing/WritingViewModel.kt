package com.mbj.ssassamarket.ui.writing

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.data.source.remote.PostItemRepository
import com.mbj.ssassamarket.util.CategoryFormat.getCategoryLabelFromInput
import com.mbj.ssassamarket.util.Constants.CATEGORY_REQUEST
import com.mbj.ssassamarket.util.Constants.WRITING_POST_ERROR
import com.mbj.ssassamarket.util.Constants.WRITING_POST_REQUEST_ALL
import com.mbj.ssassamarket.util.Constants.WRITING_POST_REQUEST_CONTENT
import com.mbj.ssassamarket.util.Constants.WRITING_POST_REQUEST_PRICE
import com.mbj.ssassamarket.util.Constants.WRITING_POST_REQUEST_TITLE
import com.mbj.ssassamarket.util.Constants.WRITING_POST_SUCCESS
import com.mbj.ssassamarket.util.Event
import kotlinx.coroutines.launch

class WritingViewModel(private val postItemRepository: PostItemRepository) : ViewModel() {

    private val _selectedImageList: MutableLiveData<List<ImageContent>> = MutableLiveData()
    val selectedImageList: LiveData<List<ImageContent>>
        get() = _selectedImageList

    private val _category = MutableLiveData<String>()
    val category: LiveData<String>
        get() = _category

    private val _latLngString = MutableLiveData<String>()
    val latLngString: LiveData<String>
        get() = _latLngString

    private val _location = MutableLiveData<String>()
    val location: LiveData<String>
        get() = _location

    val title = MutableLiveData<String>()

    val price = MutableLiveData<String>()

    val content = MutableLiveData<String>()

    private val _productUploadCompleted = MutableLiveData(true)
    val productUploadCompleted: LiveData<Boolean>
        get() = _productUploadCompleted

    private val _productUploadResponse = MutableLiveData<Boolean>()
    val productUploadResponse: LiveData<Boolean>
        get() = _productUploadResponse

    private val _productUploadSuccess = MutableLiveData<Event<Boolean>>()
    val productUploadSuccess: LiveData<Event<Boolean>>
        get() = _productUploadSuccess

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>>
        get() = _toastMessage

    fun handleGalleryResult(result: List<ImageContent>) {
        val currentList = _selectedImageList.value.orEmpty()
        val totalImages = currentList.size + result.size
        val availableCapacity = 10 - currentList.size

        val combinedList = if (totalImages <= 10) {
            currentList + result
        } else if (availableCapacity > 0) {
            currentList + result.take(availableCapacity)
        } else {
            currentList
        }
        _selectedImageList.value = combinedList.take(10)
    }

    fun removeSelectedImage(imageContent: ImageContent) {
        _selectedImageList.value = _selectedImageList.value.orEmpty()
            .filter { it != imageContent }
    }

    fun setCategoryLabel(category: String) {
        _category.value = getCategoryLabelFromInput(category)
    }

    fun setLatLng(latLng: String) {
        _latLngString.value = latLng
    }

    fun setLocation(location: String) {
        _location.value = location
    }

    fun handlePostResponse(responseBoolean: Boolean) {
        if (responseBoolean) {
            _productUploadSuccess.value = Event(true)
            _toastMessage.value = Event(WRITING_POST_SUCCESS)
        } else {
            _productUploadSuccess.value = Event(false)
            _toastMessage.value = Event(WRITING_POST_ERROR)
        }
        _productUploadCompleted.value = true
    }

    fun registerProductWithValidation() {
        val requiredPropertyCount = listOf(title.value, price.value, content.value)
            .count { it.isNullOrEmpty() } + if (category.value == CATEGORY_REQUEST) 1 else 0
        when (requiredPropertyCount) {
            0 -> {
                viewModelScope.launch {
                    _productUploadCompleted.value = false
                    _productUploadResponse.value = postItemRepository.addProductPost(
                        title = title.value!!,
                        category = category.value!!,
                        price = price.value!!,
                        content = content.value!!,
                        imageLocations = selectedImageList.value.orEmpty(),
                        location = location.value!!,
                        latLng = latLngString.value!!,
                        soldOut = false,
                        favoriteCount = 0,
                        shoppingList = List<String?>(0) { null },
                        favoriteList = List<String?>(0) { null }
                    )
                }
            }
            1 -> {
                if (title.value.isNullOrEmpty())
                    _toastMessage.value = Event(WRITING_POST_REQUEST_TITLE)
                else if (price.value.isNullOrEmpty())
                    _toastMessage.value = Event(WRITING_POST_REQUEST_PRICE)
                else if (content.value.isNullOrEmpty())
                    _toastMessage.value = Event(WRITING_POST_REQUEST_CONTENT)
                else if (category.value == CATEGORY_REQUEST)
                    _toastMessage.value = Event(CATEGORY_REQUEST)
            }
            else -> {
                _toastMessage.value = Event(WRITING_POST_REQUEST_ALL)
            }
        }
    }


    companion object {
        fun provideFactory(postItemRepository: PostItemRepository) = viewModelFactory {
            initializer {
                WritingViewModel(postItemRepository)
            }
        }
    }
}
