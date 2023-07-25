package com.mbj.ssassamarket.ui.detail.buyer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.FavoriteCountRequest
import com.mbj.ssassamarket.data.model.PatchBuyRequest
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import com.mbj.ssassamarket.util.DateFormat.getCurrentTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyerViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userInfoRepository: UserInfoRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _chatRoomId = MutableStateFlow("")
    val chatRoomId: StateFlow<String> = _chatRoomId

    val nickname: StateFlow<String> = getProductNickname().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private val _nicknameError = MutableStateFlow(false)
    val nicknameError: StateFlow<Boolean> = _nicknameError

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _likedError = MutableStateFlow(false)
    val likedError: StateFlow<Boolean> = _likedError

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _enterChatRoomError = MutableStateFlow(false)
    val enterChatRoomError: StateFlow<Boolean> = _enterChatRoomError

    private val _buyError = MutableStateFlow(false)
    val buyError: StateFlow<Boolean> = _buyError

    private val _otherUserDataError = MutableStateFlow(false)
    val otherUserDataError: StateFlow<Boolean> = _otherUserDataError

    private val _otherUserItem = MutableStateFlow<User?>(null)
    val otherUserItem: StateFlow<User?> = _otherUserItem

    private val _favoriteClicks = MutableSharedFlow<Unit>()

    private var otherUserId: String? = null
    private var postId: String? = null
    private var productPostItem: ProductPostItem? = null

    init {
        viewModelScope.launch {
            _favoriteClicks
                .conflate()
                .collectLatest {
                    toggleProductFavorite()
                }
        }
    }

    fun setOtherUserId(id: String) {
        otherUserId = id
    }

    fun getOtherUserId(): String? {
        return otherUserId
    }

    fun initializeProduct(id: String, product: ProductPostItem) {
        postId = id
        productPostItem = product
    }

    fun getProductPostItem(): ProductPostItem? {
        return productPostItem
    }

    private fun getProductNickname(): Flow<String> = userInfoRepository.getUser(
        onComplete = { _isLoading.value = false },
        onError = { _nicknameError.value = true }
    ).mapNotNull { response ->
        if (response is ApiResultSuccess) {
            val users = response.data
            val productUid = productPostItem?.id
            if (productUid != null) {
                findNicknameByUserId(users, productUid)
            } else {
                null
            }
        } else {
            null
        }
    }

    fun resetChatRoomSuccess() {
        _chatRoomId.value = ""
    }

    fun onChatButtonClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            if (productPostItem?.location != null && otherUserId != null) {
                chatRepository.enterChatRoom(
                    onComplete = { _isLoading.value = false },
                    onError = { _enterChatRoomError.value = true },
                    otherUserId!!,
                    nickname.value,
                    productPostItem?.location!!,
                    getCurrentTime(),
                ).collectLatest { chatRoomId ->
                    if (chatRoomId is ApiResultSuccess) {
                        _chatRoomId.value = chatRoomId.data

                    }
                }
            }
        }
    }

    fun onBuyButtonClicked() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val patchRequest = PatchBuyRequest(true, listOf(uId))

            if (postId != null) {
                _isLoading.value = true
                productRepository.buyProduct(
                    onComplete = { },
                    onError = { _buyError.value = true },
                    postId!!,
                    patchRequest
                ).collectLatest { response ->
                    if (response is ApiResultSuccess) {
                        if (productPostItem?.location != null && otherUserId != null) {
                            chatRepository.enterChatRoom(
                                onComplete = { _isLoading.value = false },
                                onError = { _enterChatRoomError.value = true },
                                otherUserId!!,
                                nickname.value,
                                productPostItem?.location!!,
                                getCurrentTime(),
                            ).collectLatest { chatRoomId ->
                                if (chatRoomId is ApiResultSuccess) {
                                    _chatRoomId.value = chatRoomId.data
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun toggleProductFavorite() {
        viewModelScope.launch {
            _isLoading.value = true
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""

            if (postId != null) {
                productRepository.getProductDetail(
                    onComplete = { _isLoading.value = false },
                    onError = { _likedError.value = true },
                    postId!!
                ).collectLatest { product ->
                    if (product is ApiResultSuccess) {
                        val currentFavoriteCount = product.data.favoriteCount
                        val isLiked = product.data.favoriteList?.contains(uId) == true

                        val newFavoriteCount = if (isLiked) {
                            currentFavoriteCount - 1
                        } else {
                            currentFavoriteCount + 1
                        }

                        val newFavoriteList = product.data.favoriteList.orEmpty().toMutableList().apply {
                            if (isLiked) {
                                remove(uId)
                            } else {
                                add(uId)
                            }
                        }

                        val request = FavoriteCountRequest(newFavoriteCount, newFavoriteList)
                        productRepository.updateProductFavorite(
                            onComplete = { _isLoading.value = false },
                            onError = { _likedError.value = true },
                            postId!!,
                            request
                        ).collectLatest { response ->
                            if (response is ApiResultSuccess) {
                                _isLiked.value = !isLiked
                            }
                        }
                    }
                }
            }
        }
    }

    fun checkProductInFavorites() {
        viewModelScope.launch {
            _isLoading.value = (true)
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            if (postId != null) {
                productRepository.getProductDetail(
                    onComplete = { _isLoading.value = false },
                    onError = { _likedError.value = true },
                    postId!!
                ).collectLatest { product ->
                    if (product is ApiResultSuccess) {
                        _isLiked.value = product.data.favoriteList?.contains(uId) == true
                    }
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

    fun getOtherUserItem(userId: String) {
        viewModelScope.launch {
            chatRepository.getOtherUserItem(
                onComplete = { _isLoading.value = false },
                onError = { _otherUserDataError.value = true },
                userId
            ).collectLatest { otherUser ->
                if (otherUser is ApiResultSuccess) {
                    _otherUserItem.value = otherUser.data
                }
            }
        }
    }
}
