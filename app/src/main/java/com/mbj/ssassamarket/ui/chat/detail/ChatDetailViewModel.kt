package com.mbj.ssassamarket.ui.chat.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.mbj.ssassamarket.data.model.ChatItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(private val chatRepository: ChatRepository) :
    ViewModel() {
    private val _chatRoomId = MutableLiveData<Event<String>>()
    val chatRoomId: LiveData<Event<String>> = _chatRoomId

    private val _myUserItem = MutableLiveData<Event<User>>()
    val myUserItem: LiveData<Event<User>> = _myUserItem

    private val _otherUserItem = MutableLiveData<Event<User>>()
    val otherUserItem: LiveData<Event<User>> = _otherUserItem

    private val _chatItemList = MutableLiveData<Event<List<ChatItem>>>()
    val chatItemList: LiveData<Event<List<ChatItem>>> = _chatItemList

    private var otherUserId: String? = null

    private var chatDetailEventListener: ChildEventListener? = null

    fun setChatRoomId(id: String) {
        _chatRoomId.value = Event(id)
    }

    fun setOtherUserId(id: String) {
        otherUserId = id
    }

    fun getOtherUserId(): String? {
        return otherUserId
    }

    fun getMyUserItem() {
        viewModelScope.launch {
            chatRepository.getMyUserItem() { user ->
                _myUserItem.value = Event(user)
            }
        }
    }

    fun getOtherUserItem(userId: String) {
        viewModelScope.launch {
            chatRepository.getOtherUserItem(userId) { user ->
                _otherUserItem.value = Event(user)
            }
        }
    }

    fun sendMessage(message: String) {
        val myUserName = myUserItem.value?.peekContent()?.userName?: ""
        val myUserLocation = myUserItem.value?.peekContent()?.latLng?: ""
        viewModelScope.launch {
            if (otherUserId != null) {
                chatRepository.sendMessage(
                    chatRoomId.value?.peekContent()!!,
                    otherUserId!!, message, myUserName, myUserLocation
                )
            }
        }
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
