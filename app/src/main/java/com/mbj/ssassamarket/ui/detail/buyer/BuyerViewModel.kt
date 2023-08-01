package com.mbj.ssassamarket.ui.detail.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.BuildConfig
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.*
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.NotificationRepository
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
    private val productRepository: ProductRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _chatRoomId = MutableStateFlow("")
    val chatRoomId: StateFlow<String> = _chatRoomId

    val nickname: StateFlow<String> = getProductNickname().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _buyError = MutableSharedFlow<Int>()
    val buyError: SharedFlow<Int> = _buyError.asSharedFlow()

    private val _otherUserItem = MutableStateFlow<User?>(null)
    val otherUserItem: StateFlow<User?> = _otherUserItem

    private val _myUserItem = MutableStateFlow<User?>(null)
    val myUserItem: StateFlow<User?> = _myUserItem

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
        onError = {
            viewModelScope.launch {
                _isLoading.value = false
                _buyError.emit(R.string.error_network)
            }
        }
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
            if (productPostItem?.location != null && otherUserId != null && nickname.value.isNotEmpty()) {
                chatRepository.enterChatRoom(
                    onComplete = { _isLoading.value = false },
                    onError = {
                        viewModelScope.launch {
                            _isLoading.value = false
                            _buyError.emit(R.string.error_network)
                        }
                    },
                    otherUserId!!,
                    nickname.value,
                    productPostItem?.location!!,
                    getCurrentTime(),
                ).collectLatest { chatRoomId ->
                    if (chatRoomId is ApiResultSuccess) {
                        _chatRoomId.value = chatRoomId.data
                    }
                }
            } else {
                _isLoading.value = false
                _buyError.emit(R.string.error_synchronization)
            }
        }
    }

    fun onBuyButtonClicked() {
        viewModelScope.launch {
            val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
            val patchRequest = PatchBuyRequest(true, listOf(uId))

            if (postId != null && productPostItem?.location != null && otherUserId != null && nickname.value.isNotEmpty()) {
                _isLoading.value = true
                productRepository.buyProduct(
                    onComplete = { },
                    onError = {
                        viewModelScope.launch {
                            _isLoading.value = false
                            _buyError.emit(R.string.error_network)
                        }
                    },
                    postId!!,
                    patchRequest
                ).collectLatest { response ->
                    if (response is ApiResultSuccess) {
                        chatRepository.enterChatRoom(
                            onComplete = { _isLoading.value = false },
                            onError = {
                                viewModelScope.launch {
                                    _isLoading.value = false
                                    _buyError.emit(R.string.error_network)
                                }
                            },
                            otherUserId!!,
                            nickname.value,
                            productPostItem?.location!!,
                            getCurrentTime(),
                        ).collectLatest { chatRoomId ->
                            if (chatRoomId is ApiResultSuccess) {
                                sendBuyNotificationToSeller()
                                _chatRoomId.value = chatRoomId.data
                            }
                        }
                    }
                }
            } else {
                _isLoading.value = false
                _buyError.emit(R.string.error_synchronization)
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
                    onError = {
                        viewModelScope.launch {
                            _isLoading.value = false
                            _buyError.emit(R.string.error_network)
                        }
                    },
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

                        val newFavoriteList =
                            product.data.favoriteList.orEmpty().toMutableList().apply {
                                if (isLiked) {
                                    remove(uId)
                                } else {
                                    add(uId)
                                }
                            }

                        val request = FavoriteCountRequest(newFavoriteCount, newFavoriteList)
                        productRepository.updateProductFavorite(
                            onComplete = { _isLoading.value = false },
                            onError = {
                                viewModelScope.launch {
                                    _isLoading.value = false
                                    _buyError.emit(R.string.error_network)
                                }
                            },
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
                    onError = {
                        viewModelScope.launch {
                            _isLoading.value = false
                        }
                    },
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
                onError = {
                    viewModelScope.launch {
                        _isLoading.value = false
                        _buyError.emit(R.string.error_network)
                    }
                },
                userId
            ).collectLatest { otherUser ->
                if (otherUser is ApiResultSuccess) {
                    _otherUserItem.value = otherUser.data
                }
            }
        }
    }

    fun getMyUserItem() {
        viewModelScope.launch {
            chatRepository.getMyUserItem(
                onComplete = { _isLoading.value = false },
                onError = {
                    viewModelScope.launch {
                        _isLoading.value = false
                        _buyError.emit(R.string.error_network)
                    }
                }
            ).collectLatest { myUser ->
                if (myUser is ApiResultSuccess) {
                    _myUserItem.value = myUser.data
                }
            }
        }
    }

    private suspend fun sendBuyNotificationToSeller() {
        if (otherUserItem.value?.fcmToken != null) {
            val notification = NotificationData(
                "판매 알림",
                "${myUserItem.value?.userName} 님께서 [${productPostItem?.title}] 상품을 구매하셨습니다! 얼른 채팅방을 확인해주세요 :)"
            )
            val channelType = mutableMapOf<String, String>("type" to NotificationType.SELL.label)
            val notificationRequest =
                FcmRequest(otherUserItem.value!!.fcmToken!!, "high", notification, channelType)
            notificationRepository.sendNotification(
                onComplete = { _isLoading.value = false },
                onError = {
                    viewModelScope.launch {
                        _isLoading.value = false
                        _buyError.emit(R.string.error_network)
                    }
                },
                "key=${BuildConfig.FCM_SERVER_KEY}",
                notificationRequest
            ).collectLatest {
            }
        }
    }
}
