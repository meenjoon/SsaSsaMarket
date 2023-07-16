package com.mbj.ssassamarket.ui.detail.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.EditMode
import com.mbj.ssassamarket.data.model.PatchProductRequest
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import com.mbj.ssassamarket.data.source.remote.network.onError
import com.mbj.ssassamarket.data.source.remote.network.onSuccess
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerViewModel @Inject constructor(private val userInfoRepository: UserInfoRepository, private val productRepository: ProductRepository) : ViewModel() {

    private val _product = MutableLiveData<Event<ProductPostItem?>>()
    val product: LiveData<Event<ProductPostItem?>> get() = _product

    private val _editMode = MutableLiveData(Event(EditMode.READ_ONLY))
    val editMode: LiveData<Event<EditMode>> get() = _editMode

    private val _nickname = MutableLiveData<Event<String?>>()
    val nickname: LiveData<Event<String?>> get() = _nickname

    private val _productUpdateLoading = MutableLiveData(Event(false))
    val productUpdateLoading: LiveData<Event<Boolean>> = _productUpdateLoading

    private val _productUpdateCompleted = MutableLiveData(Event(false))
    val productUpdateCompleted: LiveData<Event<Boolean>> = _productUpdateCompleted

    private val _productUpdateError = MutableLiveData(Event(false))
    val productUpdateError: LiveData<Event<Boolean>> = _productUpdateError

    private val _nicknameError = MutableStateFlow(false)
    val nicknameError: StateFlow<Boolean> = _nicknameError

    private val _nicknameLoading = MutableLiveData<Event<Boolean>>()
    val nicknameLoading: LiveData<Event<Boolean>> get() = _nicknameLoading

    private var originalProduct: ProductPostItem? = null
    private var postId: String? = null

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun initializeProduct(inputPostId: String, productPostItem: ProductPostItem) {
        if(postId == null){
            postId = inputPostId
        }
        if (_product.value == null) {
            _product.value = Event(productPostItem)
            if (originalProduct == null) {
                originalProduct = productPostItem
            }
        }
    }

    fun updateProduct(productPostItem: ProductPostItem) {
        if (editMode.value?.peekContent() == EditMode.EDIT) {
            _product.value = Event(productPostItem)
        }
    }

    fun setEditMode(editMode: EditMode) {
        _editMode.value = Event(editMode)
    }

    fun getProductNickname() {
        viewModelScope.launch {
            _nicknameLoading.value = Event(true)
            val productUid = product.value?.peekContent()?.id
            if (productUid != null) {
                userInfoRepository.getUser(
                    onComplete = { _isLoading.value = (false) },
                    onError = { _nicknameError.value = (true) }
                ).collect { response ->
                    if (response is ApiResultSuccess) {
                        val users = response.data
                        val nickname = findNicknameByUserId(users, productUid)
                        _nickname.value = Event(nickname)
                    }
                }
            } else {
                _nicknameLoading.value = Event(false)
            }
        }
    }

    fun toggleEditMode() {
        val currentEditMode = editMode.value?.peekContent()
        val newEditMode =
            if (currentEditMode == EditMode.EDIT) EditMode.READ_ONLY else EditMode.EDIT

        if (newEditMode == EditMode.READ_ONLY) {
            _product.value = Event(originalProduct)
        }

        setEditMode(newEditMode)
    }

    fun isProductModified(): Boolean {
        if (originalProduct == null) {
            return false
        }

        val currentProduct = product.value?.peekContent()

        if (currentProduct != null) {
            val isTitleModified = currentProduct.title != originalProduct!!.title
            val isPriceModified = currentProduct.price != originalProduct!!.price
            val isContentModified = currentProduct.content != originalProduct!!.content
            return isTitleModified && isPriceModified && isContentModified
        }

        return false
    }

    fun isTitleMatch(): Boolean {
        if (originalProduct == null || editMode.value?.peekContent() == EditMode.READ_ONLY) {
            return false
        }

        val currentProduct = product.value?.peekContent()
        return currentProduct?.title == originalProduct!!.title
    }

    fun isPriceMatch(): Boolean {
        if (originalProduct == null || editMode.value?.peekContent() == EditMode.READ_ONLY) {
            return false
        }

        val currentProduct = product.value?.peekContent()
        return currentProduct?.price == originalProduct!!.price
    }

    fun isContentMatch(): Boolean {
        if (originalProduct == null || editMode.value?.peekContent() == EditMode.READ_ONLY) {
            return false
        }

        val currentProduct = product.value?.peekContent()
        return currentProduct?.content == originalProduct!!.content
    }

    fun isReadOnlyMode(): Boolean {
        return editMode.value?.peekContent() == EditMode.READ_ONLY
    }

    fun updateProduct() {
        val productValue = _product.value?.peekContent()
        val originalProductValue = originalProduct

        if (productValue != null && originalProductValue != null) {
            val updatedTitle = productValue.title ?: originalProductValue.title
            val updatedPrice = productValue.price ?: originalProductValue.price
            val updatedContent = productValue.content ?: originalProductValue.content

            val patchRequest = PatchProductRequest(updatedTitle, updatedPrice, updatedContent)

            viewModelScope.launch {
                _productUpdateLoading.value = Event(true)
                if (postId != null) {
                    val result = productRepository.updateProduct(postId!!, patchRequest)
                    result.onSuccess {
                        _productUpdateLoading.value = Event(false)
                        _productUpdateCompleted.value = Event(true)
                    }.onError { code, message ->
                        _productUpdateLoading.value = Event(false)
                        _productUpdateError.value = Event(true)
                    }
                }
            }
        }
    }

    private fun findNicknameByUserId(users: Map<String, Map<String, User>>, userId: String): String? {
        val matchingUser = users.values.flatMap { it.values }.find { userInfo ->
            userInfo.userId == userId
        }
        return matchingUser?.userName
    }
}
