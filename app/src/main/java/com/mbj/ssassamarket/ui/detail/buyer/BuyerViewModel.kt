package com.mbj.ssassamarket.ui.detail.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyerViewModel @Inject constructor(private val chatRepository: ChatRepository, private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _otherUserId = MutableLiveData<Event<String>>()
    val otherUserId: LiveData<Event<String>> get() = _otherUserId

    private val _chatRoomId = MutableLiveData<Event<String>>()
    val chatRoomId: LiveData<Event<String>> get() = _chatRoomId

    private val _nickname = MutableLiveData<Event<String?>>()
    val nickname: LiveData<Event<String?>> get() = _nickname

    private var postId: String? = null
    private var productPostItem: ProductPostItem? = null

    fun setOtherUserId(id: String) {
        _otherUserId.value = Event(id)
    }

    fun initializeProduct(id: String, product: ProductPostItem) {
        postId = id
        productPostItem = product
    }

    fun getProductPostItem(): ProductPostItem? {
        return productPostItem
    }

    fun getProductNickname() {
        viewModelScope.launch {
            val productUid = productPostItem?.id
            if (productUid != null) {
                val nickname = userInfoRepository.getUserNameByUserId(productUid)
                _nickname.value = Event(nickname)
            }
        }
    }

    fun onChatButtonClicked(otherUserName: String, otherLocation: String) {
        viewModelScope.launch {
            _chatRoomId.value = Event(
                chatRepository.enterChatRoom(
                    otherUserId.value?.peekContent()!!,
                    otherUserName,
                    otherLocation
                )
            )
        }
    }
}
