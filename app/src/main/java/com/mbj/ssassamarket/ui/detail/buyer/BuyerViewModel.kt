package com.mbj.ssassamarket.ui.detail.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.source.ChatRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyerViewModel @Inject constructor(private val chatRepository: ChatRepository) : ViewModel() {

    private val _otherUserId = MutableLiveData<Event<String>>()
    val otherUserId: LiveData<Event<String>> get() = _otherUserId

    private val _chatRoomId = MutableLiveData<Event<String>>()
    val chatRoomId: LiveData<Event<String>> get() = _chatRoomId

    fun setOtherUserId(id: String) {
        _otherUserId.value = Event(id)
    }

    fun onChatButtonClicked(otherUserName: String, otherLocation: String) {
        viewModelScope.launch {
            _chatRoomId.value = Event(
                chatRepository.enterChatRoom(
                    otherUserId.value?.peekContent()!!,
                    otherUserName,
                    otherLocation
                )
            )
        }
    }
}
