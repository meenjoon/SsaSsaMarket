package com.mbj.ssassamarket.ui.detail.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.EditMode
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerViewModel @Inject constructor(private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _product = MutableLiveData<Event<ProductPostItem?>>()
    val product: LiveData<Event<ProductPostItem?>> get() = _product

    private val _editMode = MutableLiveData(Event(EditMode.READ_ONLY))
    val editMode: LiveData<Event<EditMode>> get() = _editMode

    private val _nickname = MutableLiveData<Event<String?>>()
    val nickname: LiveData<Event<String?>> get() = _nickname

    private val _productNicknameCompleted = MutableLiveData<Event<Boolean>>(Event(false))
    val productNicknameCompleted: LiveData<Event<Boolean>> get() = _productNicknameCompleted

    private val _productNicknameSuccess = MutableLiveData<Event<Boolean>>()
    val productNicknameSuccess: LiveData<Event<Boolean>> get() = _productNicknameSuccess

    private var originalProduct: ProductPostItem? = null

    fun initializeProduct(productPostItem: ProductPostItem) {
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
            val productUid = product.value?.peekContent()?.id
            if (productUid != null) {
                val nickname = userInfoRepository.getUserNameByUserId(productUid)
                _nickname.value = Event(nickname)
            }
            _productNicknameCompleted.value = Event(true)
        }
    }

    fun handlePostResponse(nickName: String?) {
        val isSuccess = nickName != null
        _productNicknameSuccess.value = Event(isSuccess)
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
}
