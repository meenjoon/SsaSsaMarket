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

    private val _product = MutableLiveData<ProductPostItem>()
    val product: LiveData<ProductPostItem> get() = _product

    private val _editMode = MutableLiveData<Event<EditMode>>()
    val editMode: LiveData<Event<EditMode>> get() = _editMode

    private val _nickname = MutableLiveData<Event<String?>>()
    val nickname: LiveData<Event<String?>> get() = _nickname

    private val _productNicknameCompleted = MutableLiveData<Boolean>()
    val productNicknameCompleted: LiveData<Boolean> get() = _productNicknameCompleted

    private val _productNicknameSuccess = MutableLiveData<Event<Boolean>>()
    val productNicknameSuccess: LiveData<Event<Boolean>> get() = _productNicknameSuccess

    init {
        _productNicknameCompleted.value = false
    }

    fun setProduct(productPostItem: ProductPostItem) {
        _product.value = productPostItem
    }

    fun setEditMode(editMode: EditMode) {
        _editMode.value = Event(editMode)
    }

    fun getProductNickname() {
        viewModelScope.launch {
            val productUid = product.value?.id
            if (productUid != null) {
                val nickname = userInfoRepository.getUserNameByUserId(productUid)
                _nickname.value = Event(nickname)
            }
            _productNicknameCompleted.value = true
        }
    }

    fun handlePostResponse(nickName: String?) {
        val isSuccess = nickName != null
        _productNicknameSuccess.value = Event(isSuccess)
    }
}
