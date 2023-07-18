package com.mbj.ssassamarket.ui.writing

import androidx.lifecycle.*
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.data.model.PatchUserLatLng
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import com.mbj.ssassamarket.util.CategoryFormat.getCategoryLabelFromInput
import com.mbj.ssassamarket.util.Constants.CATEGORY_REQUEST
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WritingViewModel @Inject constructor(
    private val postItemRepository: ProductRepository,
    private val userInfoRepository: UserInfoRepository
) : ViewModel() {

    val title = MutableStateFlow("")

    val price = MutableStateFlow("")

    val content = MutableStateFlow("")

    private val _selectedImageList = MutableStateFlow<List<ImageContent>>(emptyList())
    val selectedImageList: StateFlow<List<ImageContent>> = _selectedImageList

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isCompleted = MutableStateFlow(true)
    val isCompleted: StateFlow<Boolean> = _isCompleted

    private val _isSuccess= MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _isPostError = MutableStateFlow(false)
    val isPostError: StateFlow<Boolean> = _isPostError

    private val _toastMessage = MutableStateFlow("")
    val toastMessage: StateFlow<String> = _toastMessage

    private val _requiredProperty = MutableStateFlow(false)
    val requiredProperty: StateFlow<Boolean> = _requiredProperty

    private val _myDataId = MutableStateFlow("")
    val myDataId: StateFlow<String> = _myDataId

    private val _myDataIdError = MutableStateFlow(false)
    val myDataIdError: StateFlow<Boolean> = _myDataIdError

    private val _updateMyLatLngError = MutableStateFlow(false)
    val updateMyLatLngError: StateFlow<Boolean> = _updateMyLatLngError

    private var latLngString: String? = null

    init {
        combine(
            title,
            price,
            content,
            category,
            selectedImageList
        ) { titleValue, priceValue, contentValue, categoryValue, selectedImageListValue ->
            areAllFieldsFilled(
                titleValue,
                priceValue,
                contentValue,
                categoryValue,
                selectedImageListValue
            )
        }.distinctUntilChanged()
            .onEach { _requiredProperty.value = it }
            .launchIn(viewModelScope)
    }

    fun handleGalleryResult(result: List<ImageContent>) {
        val currentList = _selectedImageList.value
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
        _selectedImageList.value = _selectedImageList.value
            .toMutableList()
            .apply { remove(imageContent) }
    }

    fun setCategoryLabel(category: String) {
        _category.value = getCategoryLabelFromInput(category)
    }

    fun setLatLng(latLng: String) {
        latLngString = latLng
    }

    fun setLocation(location: String) {
        _location.value = location
    }

    fun registerProductWithValidation() {
        val requiredPropertyCount = listOf(title.value, price.value, content.value)
            .count { it.isNullOrEmpty() } + if (category.value == CATEGORY_REQUEST) 1 else 0 +
                if (selectedImageList.value.isNullOrEmpty()) 1 else 0
        when (requiredPropertyCount) {
            0 -> {
                viewModelScope.launch {
//                    _isLoading.value = (true)
                    _isCompleted.value = false
                    postItemRepository.addProductPost(
                        onComplete = { },
                        onError = {
                            _isPostError.value = true
                            _isCompleted.value = true
                        },
                        title = title.value,
                        category = category.value,
                        price = price.value.toInt(),
                        content = content.value,
                        imageLocations = selectedImageList.value,
                        location = location.value,
                        latLng = latLngString!!,
                        soldOut = false,
                        favoriteCount = 0,
                        shoppingList = List<String?>(0) { null },
                        favoriteList = List<String?>(0) { null }
                    ).collectLatest { response ->
                        if (response is ApiResultSuccess) {
                            updateMyLatLng(myDataId.value)
                        }
                    }
                }
            }
            1 -> {
                if (title.value.isNullOrEmpty())
                    _toastMessage.value = "request_writing_title"
                else if (price.value.isNullOrEmpty())
                    _toastMessage.value = "request_writing_price"
                else if (content.value.isNullOrEmpty())
                    _toastMessage.value = "request_writing_content"
                else if (category.value == CATEGORY_REQUEST)
                    _toastMessage.value = "request_writing_request"
                else if (selectedImageList.value.isNullOrEmpty()) {
                    _toastMessage.value = "request_writing_image"
                }
            }
            else -> {
                _toastMessage.value = "request_writing_all"
            }
        }
    }

    private fun areAllFieldsFilled(
        titleValue: String,
        priceValue: String,
        contentValue: String,
        categoryValue: String,
        selectedImageListValue: List<ImageContent>
    ): Boolean {
        val categoryRequest = categoryValue != CATEGORY_REQUEST
        return !titleValue.isNullOrEmpty() &&
                !priceValue.isNullOrEmpty() &&
                !contentValue.isNullOrEmpty() &&
                categoryRequest &&
                selectedImageListValue.isNotEmpty()
    }

    fun isPriceNullOrEmpty(price: String?): Boolean {
        return price.isNullOrEmpty()
    }

    fun getMyDataId() {
        viewModelScope.launch {
            userInfoRepository.getUser(
                onComplete = { _isLoading.value = false },
                onError = { _myDataIdError.value = true }
            ).collect { response ->
                if (response is ApiResultSuccess) {
                    val users = response.data
                    val myDataId = findMyDataId(users)
                    if (myDataId != null) {
                        _myDataId.value = myDataId
                    }
                }
            }
        }
    }

    private suspend fun findMyDataId(users: Map<String, Map<String, User>>): String? {
        val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
        for ((userNode, userDataMap) in users) {
            for ((key, foundUser) in userDataMap) {
                if (foundUser.userId == uId) {
                    return key
                }
            }
        }
        return null
    }

    private fun updateMyLatLng(dataId: String) {
        viewModelScope.launch {
            val request = PatchUserLatLng(latLngString)
            userInfoRepository.updateMyLatLng(
                onComplete = { _isLoading.value = false },
                onError = {
                    _updateMyLatLngError.value = true
                    _isCompleted.value = true
                },
                dataId,
                request
            ).collectLatest {
                _isCompleted.value = true
                _isSuccess.value = true
            }
        }
    }
}
