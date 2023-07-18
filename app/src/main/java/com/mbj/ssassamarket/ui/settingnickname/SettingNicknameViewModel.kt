package com.mbj.ssassamarket.ui.settingnickname

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import com.mbj.ssassamarket.data.source.remote.network.onError
import com.mbj.ssassamarket.data.source.remote.network.onSuccess
import com.mbj.ssassamarket.util.Constants
import com.mbj.ssassamarket.util.Constants.NICKNAME_DUPLICATE
import com.mbj.ssassamarket.util.Constants.NICKNAME_ERROR
import com.mbj.ssassamarket.util.Constants.NICKNAME_REQUEST
import com.mbj.ssassamarket.util.Constants.NICKNAME_VALID
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingNicknameViewModel @Inject constructor(private val repository: UserInfoRepository) : ViewModel() {

    val nickname = MutableLiveData<String>()

    private val _nicknameErrorMessage = MutableLiveData<Event<String>>()
    val nicknameErrorMessage: LiveData<Event<String>>
        get() = _nicknameErrorMessage

    private val _isLoading = MutableLiveData(Event(false))
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _isCompleted = MutableLiveData(Event(false))
    val isCompleted: LiveData<Event<Boolean>> = _isCompleted

    private val _isError = MutableLiveData(Event(false))
    val isError: LiveData<Event<Boolean>> = _isError

    private val _responseToastMessage = MutableLiveData<Event<String>>()
    val responseToastMessage: LiveData<Event<String>>
        get() = _responseToastMessage

    fun addUser() {
        viewModelScope.launch {
            if (nickname.value.isNullOrEmpty()) {
                _responseToastMessage.value = Event(NICKNAME_REQUEST)
            } else if (!validateNickname()) {
                _responseToastMessage.value = Event(NICKNAME_ERROR)
            } else if (isNicknameDuplicate()) {
                _responseToastMessage.value = Event(NICKNAME_DUPLICATE)
            } else {
                _isLoading.value = Event(true)
                val result = repository.addUser(nickname.value.toString())
                result.onSuccess {
                    _isLoading.value = Event(false)
                    _isCompleted.value = Event(true)
                }.onError { code, message ->
                    _isLoading.value = Event(false)
                    _isError.value = Event(true)
                }
            }
        }
    }

    fun validateNickname(): Boolean {
        val value: String = nickname.value ?: ""

        return when {
            value.isEmpty() -> {
                _nicknameErrorMessage.value = Event(NICKNAME_REQUEST)
                false
            }
            !value.matches(Constants.NICKNAME_PATTERN.toRegex()) -> {
                _nicknameErrorMessage.value = Event(NICKNAME_ERROR)
                false
            }
            else -> {
                _nicknameErrorMessage.value = Event(NICKNAME_VALID)
                true
            }
        }
    }

    private suspend fun isNicknameDuplicate(): Boolean {
        val result = repository.getUser()
        return when (result) {
            is ApiResultSuccess -> {
                val users = result.data
                users.values.flatMap { it.values }.any { userInfo ->
                    userInfo.userName == nickname.value.toString()
                }
            }
            else -> false
        }
    }
}
