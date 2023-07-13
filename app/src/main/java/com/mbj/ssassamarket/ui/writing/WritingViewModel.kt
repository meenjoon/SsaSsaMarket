package com.mbj.ssassamarket.ui.writing

import androidx.lifecycle.*
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.data.model.PatchUserLatLng
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.onError
import com.mbj.ssassamarket.data.source.remote.network.onSuccess
import com.mbj.ssassamarket.util.CategoryFormat.getCategoryLabelFromInput
import com.mbj.ssassamarket.util.Constants.CATEGORY_REQUEST
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WritingViewModel @Inject constructor(private val postItemRepository: ProductRepository, private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _selectedImageList: MutableLiveData<List<ImageContent>> = MutableLiveData()
    val selectedImageList: LiveData<List<ImageContent>>
        get() = _selectedImageList

    private val _category = MutableLiveData<String>()
    val category: LiveData<String>
        get() = _category

    private val _location = MutableLiveData<String>()
    val location: LiveData<String>
        get() = _location

    val title = MutableLiveData<String>()

    val price = MutableLiveData<String>()

    val content = MutableLiveData<String>()

    private val _isLoading = MutableLiveData(Event(false))
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _isCompleted = MutableLiveData(Event(false))
    val isCompleted: LiveData<Event<Boolean>> = _isCompleted

    private val _isPostError = MutableLiveData(Event(false))
    val isPostError: LiveData<Event<Boolean>> = _isPostError

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>>
        get() = _toastMessage

    private val _requiredProperty: MutableLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(title) { value = areAllFieldsFilled() }
        addSource(price) { value = areAllFieldsFilled() }
        addSource(content) { value = areAllFieldsFilled() }
        addSource(category) { value = areAllFieldsFilled() }
        addSource(selectedImageList) { value = areAllFieldsFilled() }
    }
    val requiredProperty: LiveData<Boolean> get() = _requiredProperty

    private val _myDataId = MutableLiveData<Event<String?>>()
    val myDataId: LiveData<Event<String?>> get() = _myDataId

    private val _myDataIdError = MutableLiveData<Event<Boolean>>()
    val myDataIdError: LiveData<Event<Boolean>> get() = _myDataIdError

    private val _updateMyLatLngError = MutableLiveData<Event<Boolean>>()
    val updateMyLatLngError: LiveData<Event<Boolean>> get() = _updateMyLatLngError

    private var latLngString : String? = null

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
                    _isLoading.value = Event(true)
                    val result =  postItemRepository.addProductPost(
                        title = title.value!!,
                        category = category.value!!,
                        price = price.value!!.toInt(),
                        content = content.value!!,
                        imageLocations = selectedImageList.value.orEmpty(),
                        location = location.value!!,
                        latLng = latLngString!!,
                        soldOut = false,
                        favoriteCount = 0,
                        shoppingList = List<String?>(0) { null },
                        favoriteList = List<String?>(0) { null }
                    )
                    result.onSuccess {
                        myDataId.value?.peekContent()?.let { updateMyLatLng(it) }
                    }.onError { code, message ->
                        _isPostError.value = Event(true)
                        _isLoading.value = Event(false)
                    }
                }
            }
            1 -> {
                if (title.value.isNullOrEmpty())
                    _toastMessage.value = Event("request_writing_title")
                else if (price.value.isNullOrEmpty())
                    _toastMessage.value = Event("request_writing_price")
                else if (content.value.isNullOrEmpty())
                    _toastMessage.value = Event("request_writing_content")
                else if (category.value == CATEGORY_REQUEST)
                    _toastMessage.value = Event("request_writing_request")
                else if (selectedImageList.value.isNullOrEmpty()) {
                    _toastMessage.value = Event("request_writing_image")
                }
            }
            else -> {
                _toastMessage.value = Event("request_writing_all")
            }
        }
    }

    private fun areAllFieldsFilled(): Boolean {
        val categoryRequest = category.value != CATEGORY_REQUEST
        return !title.value.isNullOrEmpty() &&
                !price.value.isNullOrEmpty() &&
                !content.value.isNullOrEmpty() &&
                categoryRequest &&
                selectedImageList.value?.isNotEmpty() == true
    }

    fun isPriceNullOrEmpty(price: String?): Boolean {
        return price.isNullOrEmpty()
    }

    fun getMyDataId() {
        viewModelScope.launch {
            val result = userInfoRepository.getUser()
            result.onSuccess { users ->
                val myDataId = findMyDataId(users)
                _myDataId.value = Event(myDataId)
            }.onError { code, message ->
                _myDataIdError.value = Event(true)
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
            val result = userInfoRepository.updateMyLatLng(dataId, request)
            result.onSuccess {
                _isLoading.value = Event(false)
                _isCompleted.value = Event(true)
            }.onError { code, message ->
                _updateMyLatLngError.value = Event(false)
                _isLoading.value = Event(false)
            }
        }
    }
}
