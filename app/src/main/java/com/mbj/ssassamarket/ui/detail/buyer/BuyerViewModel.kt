package com.mbj.ssassamarket.ui.detail.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.FavoriteCountRequest
import com.mbj.ssassamarket.data.model.PatchBuyRequest
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyerViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userInfoRepository: UserInfoRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _otherUserId = MutableLiveData<Event<String>>()
    val otherUserId: LiveData<Event<String>> get() = _otherUserId

    private val _chatRoomId = MutableLiveData<Event<String>>()
    val chatRoomId: LiveData<Event<String>> get() = _chatRoomId

    private val _nickname = MutableLiveData<Event<String?>>()
    val nickname: LiveData<Event<String?>> get() = _nickname

    private val _isLiked = MutableLiveData<Event<Boolean>>()
    val isLiked: LiveData<Event<Boolean>> get() = _isLiked

    private val _productFavoriteCompleted = MutableLiveData<Event<Boolean>>()
    val productFavoriteCompleted: LiveData<Event<Boolean>> get() = _productFavoriteCompleted

    private val _productFavoriteResponse = MutableLiveData<Event<Boolean>>()
    val productFavoriteResponse: LiveData<Event<Boolean>> get() = _productFavoriteResponse

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

    fun onChatButtonClicked() {
        viewModelScope.launch {
            if (nickname.value?.peekContent() != null && productPostItem?.location != null) {
                _chatRoomId.value = Event(
                    chatRepository.enterChatRoom(
                        otherUserId.value?.peekContent()!!,
                        nickname.value!!.peekContent()!!,
                        productPostItem?.location!!
                    )
                )
            }
        }
    }

    fun onBuyButtonClicked() {
        val patchRequest = PatchBuyRequest(true, listOf(productPostItem?.id))
        viewModelScope.launch {
            postId?.let { productRepository.buyProduct(it, patchRequest) }
            if (nickname.value?.peekContent() != null && productPostItem?.location != null) {
                _chatRoomId.value = Event(
                    chatRepository.enterChatRoom(
                        otherUserId.value?.peekContent()!!,
                        nickname.value!!.peekContent()!!,
                        productPostItem?.location!!
                    )
                )
            }
        }
    }

    private fun likeProduct() {
        viewModelScope.launch {
            _productFavoriteCompleted.value = Event(false)

            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val currentProductPostItem = postId?.let { productRepository.getProductDetail(it) }
            val currentFavoriteCount = currentProductPostItem?.favoriteCount

            if (currentFavoriteCount != null && postId != null) {
                val newFavoriteCount = currentFavoriteCount + 1
                val newFavoriteList = currentProductPostItem.favoriteList.orEmpty().toMutableList().apply {
                    add(uId)
                }

                val request = FavoriteCountRequest(newFavoriteCount, newFavoriteList)
                _productFavoriteResponse.value = Event(productRepository.updateProductFavorite(postId!!, request))
                toggleIsLiked()
            }
        }
    }

    private fun unlikeProduct() {
        viewModelScope.launch {
            _productFavoriteCompleted.value = Event(false)

            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val currentProductPostItem = postId?.let { productRepository.getProductDetail(it) }
            val currentFavoriteCount = currentProductPostItem?.favoriteCount

            if (currentFavoriteCount != null && postId != null) {
                val newFavoriteCount = currentFavoriteCount - 1
                val newFavoriteList = currentProductPostItem.favoriteList.orEmpty().toMutableList().apply {
                    remove(uId)
                }

                val request = FavoriteCountRequest(newFavoriteCount, newFavoriteList)
                _productFavoriteResponse.value = Event(productRepository.updateProductFavorite(postId!!, request))
                toggleIsLiked()
            }
        }
    }

    fun onHeartClicked() {
        if (isLiked.value?.peekContent() == true) {
            unlikeProduct()
        } else {
            likeProduct()
        }
    }

    private fun toggleIsLiked() {
        _isLiked.value = isLiked.value?.peekContent()?.let { Event(it.not()) }
    }

    fun checkProductInFavorites() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            _productFavoriteCompleted.value = Event(false)
            val product = postId?.let { productRepository.getProductDetail(it) }
            _productFavoriteResponse.value = Event(product?.favoriteList?.contains(uId) == true)
            _isLiked.value = productFavoriteResponse.value?.let { Event(it.peekContent()) }
        }
    }

    fun handleFavoriteResponse() {
        _productFavoriteCompleted.value = Event(true)
    }
}
