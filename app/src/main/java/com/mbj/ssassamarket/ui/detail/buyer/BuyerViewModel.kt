package com.mbj.ssassamarket.ui.detail.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.FavoriteCountRequest
import com.mbj.ssassamarket.data.model.PatchBuyRequest
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.onError
import com.mbj.ssassamarket.data.source.remote.network.onSuccess
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

    private val _nicknameError = MutableLiveData<Event<Boolean>>()
    val nicknameError: LiveData<Event<Boolean>> get() = _nicknameError

    private val _isLiked = MutableLiveData<Event<Boolean>>()
    val isLiked: LiveData<Event<Boolean>> get() = _isLiked

    private val _likedError = MutableLiveData<Event<Boolean>>()
    val likedError: LiveData<Event<Boolean>> get() = _likedError

    private val _isLoading = MutableLiveData(Event(false))
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _enterChatRoomError = MutableLiveData<Event<Boolean>>()
    val enterChatRoomError: LiveData<Event<Boolean>> get() = _enterChatRoomError

    private val _buyError = MutableLiveData<Event<Boolean>>()
    val buyError: LiveData<Event<Boolean>> get() = _buyError

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
                val result = userInfoRepository.getUser()
                result.onSuccess { users ->
                    val nickname = findNicknameByUserId(users, productUid)
                    _nickname.value = Event(nickname)
                }.onError { code, message ->
                    _nicknameError.value = Event(true)
                }
            }
        }
    }

    fun onChatButtonClicked() {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            if (nickname.value?.peekContent() != null && productPostItem?.location != null) {
                val result = chatRepository.enterChatRoom(
                    otherUserId.value?.peekContent()!!,
                    nickname.value!!.peekContent()!!,
                    productPostItem?.location!!
                )
                result.onSuccess { chatRoomId ->
                    _chatRoomId.value = Event(chatRoomId)
                    _isLoading.value = Event(false)
                }.onError { code, message ->
                    _enterChatRoomError.value = Event(true)
                    _isLoading.value = Event(false)
                }
            }
        }
    }

    fun onBuyButtonClicked() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val patchRequest = PatchBuyRequest(true, listOf(uId))

            if (postId != null) {
                _isLoading.value = Event(true)
                val result = productRepository.buyProduct(postId!!, patchRequest)
                result.onSuccess {
                    val enterChatRoomResult = chatRepository.enterChatRoom(
                        otherUserId.value?.peekContent()!!,
                        nickname.value!!.peekContent()!!,
                        productPostItem?.location!!
                    )
                    enterChatRoomResult.onSuccess { chatRoomId ->
                        _chatRoomId.value = Event(chatRoomId)
                        _isLoading.value = Event(false)
                    }.onError { code, message ->
                        _enterChatRoomError.value = Event(true)
                        _isLoading.value = Event(false)
                    }
                }.onError { code, message ->
                    _buyError.value = Event(true)
                    _isLoading.value = Event(false)
                }
            }
        }
    }

    private fun likeProduct() {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""

            if (postId != null) {
                val productResult = productRepository.getProductDetail(postId!!)
                productResult.onSuccess { product ->
                    val currentFavoriteCount = product.favoriteCount
                    val newFavoriteCount = currentFavoriteCount + 1
                    val newFavoriteList = product.favoriteList.orEmpty().toMutableList().apply {
                        add(uId)
                    }
                    val request = FavoriteCountRequest(newFavoriteCount, newFavoriteList)
                    val result = productRepository.updateProductFavorite(postId!!, request)
                    result.onSuccess {
                        toggleIsLiked()
                        _isLoading.value = Event(false)
                    }.onError { code, message ->
                        _isLoading.value = Event(false)
                        _likedError.value = Event(true)
                    }
                }.onError { code, message ->
                    _isLoading.value = Event(false)
                    _likedError.value = Event(true)
                }
            }
        }
    }

    private fun unlikeProduct() {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""

            if (postId != null) {
                val productResult = productRepository.getProductDetail(postId!!)
                productResult.onSuccess { product ->
                    val currentFavoriteCount = product.favoriteCount
                    val newFavoriteCount = currentFavoriteCount - 1
                    val newFavoriteList = product.favoriteList.orEmpty().toMutableList().apply {
                        remove(uId)
                    }
                    val request = FavoriteCountRequest(newFavoriteCount, newFavoriteList)
                    val result = productRepository.updateProductFavorite(postId!!, request)
                    result.onSuccess {
                        toggleIsLiked()
                        _isLoading.value = Event(false)
                    }.onError { code, message ->
                        _isLoading.value = Event(false)
                        _likedError.value = Event(true)
                    }
                }.onError { code, message ->
                    _isLoading.value = Event(false)
                    _likedError.value = Event(true)
                }
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
            _isLoading.value = Event(true)
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            if (postId != null) {
                val result = productRepository.getProductDetail(postId!!)
                result.onSuccess { product ->
                    _isLiked.value = Event(product.favoriteList?.contains(uId) == true)
                    _isLoading.value = Event(false)
                }.onError { code, message ->
                    _likedError.value = Event(true)
                }
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
}
