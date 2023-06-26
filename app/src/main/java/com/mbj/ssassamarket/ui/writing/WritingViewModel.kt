package com.mbj.ssassamarket.ui.writing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mbj.ssassamarket.data.model.ImageContent

class WritingViewModel : ViewModel() {

    private val _selectedImageList: MutableLiveData<List<ImageContent>> = MutableLiveData()
    val selectedImageList: LiveData<List<ImageContent>>
        get() = _selectedImageList

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
}
