package com.mbj.ssassamarket.ui.chat.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ValueEventListener
import com.mbj.ssassamarket.data.model.ChatRoomItem
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(private val chatRepository: ChatRepository) : ViewModel() {

    private val _chatRooms = MutableStateFlow<List<ChatRoomItem>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoomItem>> = _chatRooms

    private val _chatRoomsError = MutableStateFlow(false)
    val chatRoomsError: StateFlow<Boolean> = _chatRoomsError

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var chatRoomsValueEventListener: ValueEventListener? = null
    private var isLocationPermissionChecked = false
    private var isSystemSettingsExited = false

    fun isLocationPermissionChecked(): Boolean {
        return isLocationPermissionChecked
    }

    fun setLocationPermissionChecked(checked: Boolean) {
        isLocationPermissionChecked = checked
    }

    fun isSystemSettingsExited(): Boolean {
        return isSystemSettingsExited
    }

    fun setSystemSettingsExited(exited: Boolean) {
        isSystemSettingsExited = exited
    }

    fun getChatRooms() {
        viewModelScope.launch {
            chatRepository.getMyChatRooms(
                onComplete = { _isLoading.value = false },
                onError = { _chatRoomsError.value = true }
            ).collectLatest { chatRoomList ->
                if (chatRoomList is ApiResultSuccess) {
                    _chatRooms.value = chatRoomList.data
                        .sortedByDescending { chatRoomItem -> chatRoomItem.lastSentTime }
                } else {
                    _chatRoomsError.value = true
                }
            }
        }
    }

    fun addChatRoomsValueEventListener() {
        viewModelScope.launch {
            chatRoomsValueEventListener =
                chatRepository.addChatRoomsValueEventListener { chatRoomList ->
                    _chatRooms.value = chatRoomList
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
