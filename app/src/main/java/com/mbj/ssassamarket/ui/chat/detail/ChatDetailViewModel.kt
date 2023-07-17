package com.mbj.ssassamarket.ui.chat.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.mbj.ssassamarket.data.model.ChatItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import com.mbj.ssassamarket.util.DateFormat.getCurrentTime
import com.mbj.ssassamarket.util.location.LocationFormat.calculateDistance
import com.mbj.ssassamarket.util.location.LocationFormat.formatDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userInfoRepository: UserInfoRepository
) : ViewModel() {

    private val _myUserItem = MutableStateFlow<User?>(null)
    val myUserItem: StateFlow<User?> = _myUserItem

    private val _otherUserItem = MutableStateFlow<User?>(null)
    val otherUserItem: StateFlow<User?> = _otherUserItem

    private val _chatItemList = MutableStateFlow<List<ChatItem>>(emptyList())
    val chatItemList: StateFlow<List<ChatItem>> = _chatItemList

    private val _distanceDiff = MutableStateFlow("")
    val distanceDiff: StateFlow<String> = _distanceDiff

    private val _latLngString = MutableStateFlow("")
    val latLngString: StateFlow<String> = _latLngString

    private val _myDataId = MutableStateFlow("")
    val myDataId: StateFlow<String> = _myDataId

    private val _myDataIdError = MutableStateFlow(false)
    val myDataIdError: StateFlow<Boolean> = _myDataIdError

    private val _myUserDataError = MutableStateFlow(false)
    val myUserDataError: StateFlow<Boolean> = _myUserDataError

    private val _otherUserDataError = MutableStateFlow(false)
    val otherUserDataError: StateFlow<Boolean> = _otherUserDataError

    private val _sendMessageError = MutableStateFlow(false)
    val sendMessageError: StateFlow<Boolean> = _sendMessageError

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val otherLocation = MutableStateFlow("")

    private var otherUserId: String? = null
    private var chatRoomId: String? = null
    private var location: String? = null
    private var latLngState: String? = null
    private var otherUserItemState: User? = null
    private var chatDetailEventListener: ChildEventListener? = null

    fun setChatRoomId(id: String) {
        chatRoomId = id
    }

    fun setOtherUserId(id: String) {
        otherUserId = id
    }

    fun setLatLng(latLng: String) {
        _latLngString.value = latLng
    }

    fun setLngState(State: String) {
        latLngState = State
    }

    fun setOtherUserItemState(state: User) {
        otherUserItemState = state
    }

    fun setLocation(loc: String) {
        location = loc
    }

    fun setOtherLocation(otherLoc: String) {
        otherLocation.value = otherLoc
    }

    fun getMyUserItem() {
        viewModelScope.launch {
            chatRepository.getMyUserItem(
                onComplete = { _isLoading.value = false },
                onError = { _myUserDataError.value = true }
            ).collectLatest { myUser ->
                if (myUser is ApiResultSuccess) {
                    _myUserItem.value = myUser.data
                }
            }
        }
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

    fun sendMessage(message: String, myDataId: String) {
        val myUserName = myUserItem.value?.userName ?: ""
        val myUserLocation = location
        val myLatLng = latLngString.value
        viewModelScope.launch {
            if (otherUserId != null && chatRoomId != null && myUserLocation != null) {
                chatRepository.sendMessage(
                    onComplete = { _isLoading.value = false },
                    onError = { _sendMessageError.value = true },
                    chatRoomId!!,
                    otherUserId!!,
                    message,
                    myUserName,
                    myUserLocation,
                    getCurrentTime(),
                    myLatLng,
                    myDataId
                ).collectLatest {
                }
            }
        }
    }

    fun processLocationData() {
        if (latLngState != null && otherUserItemState != null) {
            val myLatLng = latLngString.value
            val otherLatLng = otherUserItem.value?.latLng

            val myCoordinates = myLatLng.split(" ")
            val myLatitude = myCoordinates.get(0).toDouble()
            val myLongitude = myCoordinates.get(1).toDouble()

            val otherCoordinates = otherLatLng?.split(" ")
            val otherLatitude = otherCoordinates?.get(0)?.toDouble()
            val otherLongitude = otherCoordinates?.get(1)?.toDouble()

            // 거리 계산 및 관련 로직 수행
            if (myLatitude != null && myLongitude != null && otherLatitude != null && otherLongitude != null) {
                val distance = calculateDistance(myLatitude, myLongitude, otherLatitude, otherLongitude)
                val distanceFormat = formatDistance(distance)
                _distanceDiff.value = distanceFormat
            }
        }
    }

    fun getMyDataId() {
        viewModelScope.launch {
            userInfoRepository.getUser(
                onComplete = { _isLoading.value = false },
                onError = { _myDataIdError.value = true }
            ).collectLatest { response ->
                if (response is ApiResultSuccess) {
                    val users = response.data
                    val myDataId = findMyDataId(users)
                    if (myDataId != null) {
                        _myDataId.value = myDataId
                    }
                }
            }
        }
    }


    private suspend fun findMyDataId(users: Map<String, Map<String, User>>): String? {
        val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
        for ((userNode, userDataMap) in users) {
            for ((key, foundUser) in userDataMap) {
                if (foundUser.userId == uId) {
                    return key
                }
            }
        }
        return null
    }

    fun addChatDetailEventListener() {
        if (chatRoomId != null) {
            chatDetailEventListener = chatRoomId.let {
                chatRepository.addChatDetailEventListener(it!!) { chatItem ->
                    val currentList = _chatItemList.value
                    val newList = currentList.toMutableList().apply { add(chatItem) }
                    _chatItemList.value = newList
                }
            }
        }
    }

    fun removeChatDetailEventListener() {
        if (chatRoomId != null) {
            chatDetailEventListener?.let {
                chatRepository.removeChatDetailEventListener(it, chatRoomId!!)
            }
        }
        chatDetailEventListener = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            removeChatDetailEventListener()
        }
    }
}
