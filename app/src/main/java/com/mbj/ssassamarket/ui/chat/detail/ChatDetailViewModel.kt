package com.mbj.ssassamarket.ui.chat.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.mbj.ssassamarket.data.model.ChatItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import com.mbj.ssassamarket.data.source.remote.network.onError
import com.mbj.ssassamarket.data.source.remote.network.onSuccess
import com.mbj.ssassamarket.util.DateFormat.getCurrentTime
import com.mbj.ssassamarket.util.Event
import com.mbj.ssassamarket.util.location.LocationFormat.calculateDistance
import com.mbj.ssassamarket.util.location.LocationFormat.formatDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userInfoRepository: UserInfoRepository
) : ViewModel() {

    private val _chatRoomId = MutableLiveData<Event<String>>()
    val chatRoomId: LiveData<Event<String>> = _chatRoomId

    private val _myUserItem = MutableLiveData<Event<User>>()
    val myUserItem: LiveData<Event<User>> = _myUserItem

    private val _otherUserItem = MutableLiveData<Event<User>>()
    val otherUserItem: LiveData<Event<User>> = _otherUserItem

    private val _chatItemList = MutableLiveData<Event<List<ChatItem>>>()
    val chatItemList: LiveData<Event<List<ChatItem>>> = _chatItemList

    private val _distanceDiff = MutableLiveData<Event<String>>()
    val distanceDiff: LiveData<Event<String>> = _distanceDiff

    private var otherUserId: String? = null

    private var chatDetailEventListener: ChildEventListener? = null

    private val _latLngString = MutableLiveData<Event<String>>()
    val latLngString: LiveData<Event<String>>
        get() = _latLngString

    private val _location = MutableLiveData<Event<String>>()
    val location: LiveData<Event<String>>
        get() = _location

    private val _myDataId = MutableStateFlow("")
    val myDataId: StateFlow<String> = _myDataId

    private val _myDataIdError = MutableLiveData(false)
    val myDataIdError: LiveData<Boolean> = _myDataIdError

    private val _myUserDataError = MutableLiveData<Event<Boolean>>()
    val myUserDataError: LiveData<Event<Boolean>> get() = _myUserDataError

    private val _otherUserDataError = MutableLiveData<Event<Boolean>>()
    val otherUserDataError: LiveData<Event<Boolean>> get() = _otherUserDataError

    private val _sendMessageError = MutableLiveData<Event<Boolean>>()
    val sendMessageError: LiveData<Event<Boolean>> get() = _sendMessageError

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var latLngState: String? = null
    private var otherUserItemState: User? = null

    fun setChatRoomId(id: String) {
        _chatRoomId.value = Event(id)
    }

    fun setOtherUserId(id: String) {
        otherUserId = id
    }

    fun setLatLng(latLng: String) {
        _latLngString.value = Event(latLng)
    }

    fun setLngState(State: String) {
        latLngState = State
    }

    fun setOtherUserItemState(state: User) {
        otherUserItemState = state
    }

    fun setLocation(location: String) {
        _location.value = Event(location)
    }

    fun getMyUserItem() {
        viewModelScope.launch {
            val result = chatRepository.getMyUserItem()
            result.onSuccess { user ->
                _myUserItem.value = Event(user)
            }.onError { code, message ->
                _myUserDataError.value = Event(true)
            }
        }
    }

    fun getOtherUserItem(userId: String) {
        viewModelScope.launch {
            val result = chatRepository.getOtherUserItem(userId)
            result.onSuccess { otherUser ->
                _otherUserItem.value = Event(otherUser)
            }.onError { code, message ->
                _otherUserDataError.value = Event(true)
            }
        }
    }

    fun sendMessage(message: String, myDataId: String) {
        val myUserName = myUserItem.value?.peekContent()?.userName ?: ""
        val myUserLocation = location.value?.peekContent() ?: ""
        val myLatLng = latLngString.value?.peekContent() ?: ""
        viewModelScope.launch {
            if (otherUserId != null) {
                val result = chatRepository.sendMessage(
                    chatRoomId.value?.peekContent()!!,
                    otherUserId!!,
                    message,
                    myUserName,
                    myUserLocation,
                    getCurrentTime(),
                    myLatLng,
                    myDataId
                )
                result.onSuccess {
                }.onError { code, message ->
                    _sendMessageError.value = Event(true)
                }
            }
        }
    }

    fun processLocationData() {
        if (latLngState != null && otherUserItemState != null) {
            val myLatLng = latLngString.value?.peekContent()
            val otherLatLng = otherUserItem.value?.peekContent()?.latLng

            val myCoordinates = myLatLng?.split(" ")
            val myLatitude = myCoordinates?.get(0)?.toDouble()
            val myLongitude = myCoordinates?.get(1)?.toDouble()

            val otherCoordinates = otherLatLng?.split(" ")
            val otherLatitude = otherCoordinates?.get(0)?.toDouble()
            val otherLongitude = otherCoordinates?.get(1)?.toDouble()

            // 거리 계산 및 관련 로직 수행
            if (myLatitude != null && myLongitude != null && otherLatitude != null && otherLongitude != null) {
                val distance =
                    calculateDistance(myLatitude, myLongitude, otherLatitude, otherLongitude)
                val distanceFormat = formatDistance(distance)
                _distanceDiff.value = Event(distanceFormat)
            }
        }
    }

    fun getMyDataId() {
        viewModelScope.launch {
            userInfoRepository.getUser(
                onComplete = { _isLoading.value = false},
                onError = { _myDataIdError.value = true }
            ).collect { response ->
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
        chatDetailEventListener = chatRoomId.value?.peekContent()?.let {
            chatRepository.addChatDetailEventListener(it) { chatItem ->
                val currentList = _chatItemList.value?.peekContent() ?: emptyList()
                val newList = currentList.toMutableList().apply { add(chatItem) }
                _chatItemList.value = Event(newList)
            }
        }
    }

    fun removeChatDetailEventListener() {
        chatDetailEventListener?.let {
            chatRepository.removeChatDetailEventListener(it, _chatRoomId.value?.peekContent() ?: "")
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
