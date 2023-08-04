package com.mbj.ssassamarket.ui.writing

import androidx.lifecycle.*
import com.mbj.ssassamarket.R
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

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _isWritingError = MutableSharedFlow<Int>()
    val isPostError: SharedFlow<Int> = _isWritingError.asSharedFlow()

    private val _toastMessageId = MutableSharedFlow<Int>()
    val toastMessageId: SharedFlow<Int> = _toastMessageId.asSharedFlow()

    private val _requiredProperty = MutableStateFlow(false)
    val requiredProperty: StateFlow<Boolean> = _requiredProperty

    private val myDataId: StateFlow<String> = getMyDataId().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private var latLngString: String? = null
    private var isLocationPermissionChecked = false
    private var isSystemSettingsExited = false

    init {
        viewModelScope.launch {
            myDataId.collectLatest {
            }
        }
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

    fun isLocationPermissionChecked(): Boolean {
        return isLocationPermissionChecked
    }

    fun setLocationPermissionChecked(checked: Boolean) {
        isLocationPermissionChecked = checked
    }

    fun isSystemSettingsExited(): Boolean {
        return isSystemSettingsExited
    }

    fun setSystemSettingsExited(exited: Boolean) {
        isSystemSettingsExited = exited
    }

    fun setCategoryLabel(category: String) {
        _category.value = getCategoryLabelFromInput(category)
    }

    fun setLatLng(latLng: String) {
        latLngString = latLng
    }

    fun setLocation(location: String) {
        _isLoading.value = false
        _location.value = location
    }

    fun registerProductWithValidation() {
        if (location.value.isNotEmpty()) {
            val requiredPropertyCount = listOf(title.value, price.value, content.value)
                .count { it.isEmpty() } + if (category.value == CATEGORY_REQUEST) 1 else 0 +
                    if (selectedImageList.value.isEmpty()) 1 else 0
            viewModelScope.launch {
                when (requiredPropertyCount) {
                    0 -> {
                        _isCompleted.value = false
                        postItemRepository.addProductPost(
                            onComplete = { },
                            onError = {
                                _isCompleted.value = true
                                viewModelScope.launch {
                                    _isWritingError.emit(R.string.error_network)
                                }
                            },
                            title = title.value,
                            category = category.value,
                            price = price.value.toLong(),
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

                    1 -> {
                        if (title.value.isEmpty())
                            _toastMessageId.emit(R.string.request_writing_title)
                        else if (price.value.isEmpty())
                            _toastMessageId.emit(R.string.request_writing_price)
                        else if (content.value.isEmpty())
                            _toastMessageId.emit(R.string.request_writing_content)
                        else if (category.value == CATEGORY_REQUEST)
                            _toastMessageId.emit(R.string.request_writing_request)
                        else if (selectedImageList.value.isEmpty()) {
                            _toastMessageId.emit(R.string.request_writing_image)
                        }
                    }

                    else -> {
                        _toastMessageId.emit(R.string.request_writing_all)
                    }
                }
            }
        } else {
            viewModelScope.launch {
                _isWritingError.emit(R.string.error_synchronization)
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
        return !titleValue.isEmpty() &&
                !priceValue.isEmpty() &&
                !contentValue.isEmpty() &&
                categoryRequest &&
                selectedImageListValue.isNotEmpty()
    }

    fun isPriceNullOrEmpty(price: String?): Boolean {
        return price.isNullOrEmpty()
    }

    private fun getMyDataId(): Flow<String> = userInfoRepository.getUser(
        onComplete = { _isLoading.value = false },
        onError = {
            viewModelScope.launch {
                _isWritingError.emit(R.string.error_network)
            }
        }
    ).mapNotNull { response ->
        if (response is ApiResultSuccess) {
            val users = response.data
            val myDataId = findMyDataId(users)
            myDataId
        } else {
            null
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
                    _isCompleted.value = true
                    viewModelScope.launch {
                        _isWritingError.emit(R.string.error_network)
                    }
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
