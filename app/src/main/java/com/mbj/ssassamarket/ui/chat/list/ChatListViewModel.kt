package com.mbj.ssassamarket.ui.chat.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ValueEventListener
import com.mbj.ssassamarket.data.model.ChatRoomItem
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.remote.network.onError
import com.mbj.ssassamarket.data.source.remote.network.onSuccess
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(private val chatRepository: ChatRepository) : ViewModel() {

    private val _chatRooms = MutableLiveData<Event<List<ChatRoomItem>>>()
    val chatRooms: LiveData<Event<List<ChatRoomItem>>> = _chatRooms

    private val _chatRoomsError = MutableLiveData<Event<Boolean>>()
    val chatRoomsError: LiveData<Event<Boolean>> get() = _chatRoomsError

    private var chatRoomsValueEventListener: ValueEventListener? = null

    fun getChatRooms() {
        viewModelScope.launch {
            val result = chatRepository.getChatRooms()
            result.onSuccess { chatRoomList ->
                _chatRooms.value = Event(chatRoomList)
            }.onError { code, message ->
                _chatRoomsError.value = Event(true)
            }
        }
    }

    fun addChatRoomsValueEventListener() {
        viewModelScope.launch {
            chatRoomsValueEventListener =
                chatRepository.addChatRoomsValueEventListener { chatRoomList ->
                    _chatRooms.value = Event(chatRoomList)
                }
        }
    }

    fun removeChatRoomsValueEventListener() {
        viewModelScope.launch {
            chatRoomsValueEventListener?.let {
                chatRepository.removeChatRoomsValueEventListener(it)
            }
            chatRoomsValueEventListener = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            removeChatRoomsValueEventListener()
        }
    }
}
