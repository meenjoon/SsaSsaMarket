package com.mbj.ssassamarket.ui.writing

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.data.source.remote.PostItemRepository
import com.mbj.ssassamarket.util.CategoryFormat.getCategoryLabelFromInput

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

    companion object {
        fun provideFactory(postItemRepository: PostItemRepository) = viewModelFactory {
            initializer {
                WritingViewModel(postItemRepository)
            }
        }
    }
}
