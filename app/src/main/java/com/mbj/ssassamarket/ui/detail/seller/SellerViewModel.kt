package com.mbj.ssassamarket.ui.detail.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.EditMode
import com.mbj.ssassamarket.data.model.PatchProductRequest
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerViewModel @Inject constructor(private val userInfoRepository: UserInfoRepository, private val productRepository: ProductRepository) : ViewModel() {

    private val _product = MutableStateFlow<ProductPostItem?>(null)
    val product: StateFlow<ProductPostItem?> = _product

    private val _editMode = MutableStateFlow(EditMode.READ_ONLY)
    val editMode: StateFlow<EditMode> = _editMode

    val nickname: StateFlow<String> = getProductNickname().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _productUpdateCompleted = MutableStateFlow(false)
    val productUpdateCompleted: StateFlow<Boolean> = _productUpdateCompleted

    private val _productUpdateError = MutableStateFlow(false)
    val productUpdateError: StateFlow<Boolean> = _productUpdateError

    private val _nicknameError = MutableStateFlow(false)
    val nicknameError: StateFlow<Boolean> = _nicknameError

    private val _isProductInfoUnchanged = MutableStateFlow(false)
    val isProductInfoUnchanged: StateFlow<Boolean> = _isProductInfoUnchanged

    private val _productDeleteError = MutableStateFlow(false)
    val productDeleteError: StateFlow<Boolean> = _productDeleteError

    private val _productDeleteSuccess = MutableStateFlow(false)
    val productDeleteSuccess: StateFlow<Boolean> = _productDeleteSuccess

    private val _isInvalidProductInfoClicked = MutableSharedFlow<Unit>()
    val isInvalidProductInfoClicked: SharedFlow<Unit> = _isInvalidProductInfoClicked.asSharedFlow()

    private var originalProduct: ProductPostItem? = null

    private var postId: String? = null

    fun initializeProduct(inputPostId: String, productPostItem: ProductPostItem) {
        if (postId == null) {
            postId = inputPostId
        }
        if (_product.value == null) {
            _product.value = productPostItem
            if (originalProduct == null) {
                originalProduct = productPostItem
            }
        }
    }

    private fun setEditMode(editMode: EditMode) {
        _editMode.value = editMode
    }

    private fun getProductNickname(): Flow<String> = userInfoRepository.getUser(
        onComplete = { _isLoading.value = false },
        onError = { _nicknameError.value = true }
    ).mapNotNull { response ->
        val productUid = product.value?.id
        if (productUid != null && response is ApiResultSuccess) {
            val users = response.data
            findNicknameByUserId(users, productUid)
        } else {
            null
        }
    }

    fun toggleEditMode() {
        val currentEditMode = editMode.value
        val newEditMode =
            if (currentEditMode == EditMode.EDIT) EditMode.READ_ONLY else EditMode.EDIT
        if (newEditMode == EditMode.READ_ONLY) {
            _product.value = null
            _product.value = originalProduct
        }
        setEditMode(newEditMode)
    }

    fun isReadOnlyMode(): Boolean {
        return editMode.value == EditMode.READ_ONLY
    }

    fun updateProduct(title: String, price: String, content: String) {
        if (title.isNullOrBlank() || price.isNullOrBlank() || content.isNullOrBlank()) {
            viewModelScope.launch {
                _isInvalidProductInfoClicked.emit(Unit)
            }
        } else {
            val productValue = _product.value
            val patchRequest = PatchProductRequest(title, price.toLong(), content)
            if (isProductInfoChanged(productValue, title, price, content)) {
                viewModelScope.launch {
                    _isLoading.value = true
                    if (postId != null) {
                        productRepository.updateProduct(
                            onComplete = { _isLoading.value = false },
                            onError = { _productUpdateError.value = true },
                            postId!!,
                            patchRequest
                        ).collectLatest { response ->
                            if (response is ApiResultSuccess) {
                                _productUpdateCompleted.value = true
                            }
                        }
                    } else {
                        _productUpdateError.value = true
                    }
                }
            } else {
                _isProductInfoUnchanged.value = true
            }
        }
    }

    private fun findNicknameByUserId(
        users: Map<String, Map<String, User>>,
        userId: String
    ): String? {
        val matchingUser = users.values.flatMap { it.values }.find { userInfo ->
            userInfo.userId == userId
        }
        return matchingUser?.userName
    }

    private fun isProductInfoChanged(
        product: ProductPostItem?,
        title: String,
        price: String,
        content: String
    ): Boolean {
        return title != product?.title || price.toLong() != product.price || content != product.content
    }

    fun deleteProductData() {
        _isLoading.value = true
        viewModelScope.launch {
            if (postId != null) {
                productRepository.deleteProductData(
                    onComplete = { _isLoading.value = false },
                    onError = { _productDeleteError.value = true },
                    postId!!
                ).collectLatest { response ->
                    if (response is ApiResultSuccess) {
                        _productDeleteSuccess.value = true
                    }
                }
            }
        }
    }
}
