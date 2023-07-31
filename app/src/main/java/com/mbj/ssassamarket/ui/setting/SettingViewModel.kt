package com.mbj.ssassamarket.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.ChatRoomItem
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.ProductRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    private val productRepository: ProductRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLogoutSuccess = MutableStateFlow(false)
    val isLogoutSuccess: StateFlow<Boolean> = _isLogoutSuccess

    private val _isLogoutError = MutableStateFlow(false)
    val isLogoutError: StateFlow<Boolean> = _isLogoutError

    private val _isMembershipWithdrawalSuccess = MutableStateFlow(false)
    val isMembershipWithdrawalSuccess: StateFlow<Boolean> = _isMembershipWithdrawalSuccess

    private val _isMembershipWithdrawalError = MutableStateFlow(false)
    val isMembershipWithdrawalError: StateFlow<Boolean> = _isMembershipWithdrawalError

    private val _isMembershipWithdrawalCompleted = MutableStateFlow<Boolean?>(null)
    val isMembershipWithdrawalCompleted: StateFlow<Boolean?> = _isMembershipWithdrawalCompleted

    private var myUid: String? = null

    init {
        getMyUid()
    }

    fun onLogoutButtonClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            userInfoRepository.logout(
                onComplete = { _isLoading.value = false },
                onError = { _isLogoutError.value = true }
            ).collectLatest { response ->
                if (response is ApiResultSuccess) {
                    _isLogoutSuccess.value = true
                }
            }
        }
    }

    private fun getMyUid() {
        viewModelScope.launch {
            myUid = userInfoRepository.getUserAndIdToken().first?.uid
        }
    }

    fun onMembershipWithdrawalClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            _isMembershipWithdrawalCompleted.value = false
            deleteChatRoomData()
            deleteProductData()
            performLogout()
            deleteUserData()
        }
    }

    private suspend fun deleteUserData() {
        _isLoading.value = true
        if (myUid != null) {
            userInfoRepository.deleteUserData(
                onComplete = { _isLoading.value = false },
                onError = { _isMembershipWithdrawalError.value = true },
                myUid!!
            ).collectLatest { response ->
                if (response is ApiResultSuccess) {
                    _isMembershipWithdrawalCompleted.value = true
                    _isMembershipWithdrawalSuccess.value = true
                }
            }
        }
    }

    private suspend fun deleteProductData() {
        _isLoading.value = true
        productRepository.getProduct(
            onComplete = { _isLoading.value = false },
            onError = { _isMembershipWithdrawalError.value = true }
        ).collectLatest { response ->
            if (response is ApiResultSuccess) {
                response.data.forEach { (postId, postData) ->
                    if (postData.id == myUid) {
                        _isLoading.value = true
                        productRepository.deleteProductData(
                            onComplete = { _isLoading.value = false },
                            onError = { _isMembershipWithdrawalError.value = true },
                            postId
                        ).collectLatest {
                        }
                    }
                }
            }
        }
    }

    private suspend fun deleteChatRoomData() {
        _isLoading.value = true
        chatRepository.getAllChatRoomData(
            onComplete = { },
            onError = { _isMembershipWithdrawalError.value = true },
        ).collectLatest { response ->
            if (response is ApiResultSuccess) {
                response.data.forEach { (uid, chatRoomData) ->

                    if (uid == myUid) {
                        deleteChatRoomsDataForMyUid(uid)
                        deleteChatMessagesForChatRooms(chatRoomData.values)
                    }

                    if (chatRoomData.keys.contains(myUid)) {
                        chatRoomData.keys.forEach { otherUserUid ->
                            if (otherUserUid == myUid) {
                                deleteMyInfoFromChatRoomsForOtherUser(uid, otherUserUid)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun deleteChatRoomsDataForMyUid(uid: String) {
        chatRepository.deleteChatRoomsDataForMyUid(
            onComplete = { _isLoading.value = false },
            onError = { _isMembershipWithdrawalError.value = true },
            uid
        ).collectLatest {
        }
    }

    private suspend fun deleteMyInfoFromChatRoomsForOtherUser(uid: String, otherUserUid: String) {
        chatRepository.deleteMyInfoFromChatRoomsForOtherUser(
            onComplete = { _isLoading.value = false },
            onError = { _isMembershipWithdrawalError.value = true },
            uid,
            otherUserUid
        ).collectLatest {
        }
    }

    private suspend fun deleteChatMessagesForChatRooms(chatRooms: Collection<ChatRoomItem>) {
        chatRooms.forEach { chatRoomItem ->
            chatRoomItem.chatRoomId?.let { chatRoomId ->
                chatRepository.deleteChatMessageByChatRoomId(
                    onComplete = { _isLoading.value = false },
                    onError = { _isMembershipWithdrawalError.value = true },
                    chatRoomId
                ).collectLatest {
                }
            }
        }
    }

    private suspend fun performLogout() {
        _isLoading.value = true
        userInfoRepository.logout(
            onComplete = { _isLoading.value = false },
            onError = { _isLogoutError.value = true }
        ).collectLatest {
        }
    }
}
