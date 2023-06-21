package com.mbj.ssassamarket.ui.settingnickname

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.util.Constants
import com.mbj.ssassamarket.util.Constants.NICKNAME_DUPLICATE
import com.mbj.ssassamarket.util.Constants.NICKNAME_ERROR
import com.mbj.ssassamarket.util.Constants.NICKNAME_REQUEST
import com.mbj.ssassamarket.util.Constants.NICKNAME_VALID
import com.mbj.ssassamarket.util.Constants.SUCCESS
import com.mbj.ssassamarket.util.Event
import kotlinx.coroutines.launch

class SettingNicknameViewModel(private val repository: UserInfoRepository) : ViewModel() {

    val nickname = MutableLiveData<String>()

    private val _nicknameErrorMessage = MutableLiveData<String>()
    val nicknameErrorMessage: LiveData<String>
        get() = _nicknameErrorMessage

    private val _addUserResult = MutableLiveData<Boolean>()
    val addUserResult: LiveData<Boolean>
        get() = _addUserResult

    private val _preUploadCompleted = MutableLiveData<Boolean>()
    val preUploadCompleted: LiveData<Boolean>
        get() = _preUploadCompleted

    private val _uploadSuccess = MutableLiveData<Event<Boolean>>()
    val uploadSuccess: LiveData<Event<Boolean>>
        get() = _uploadSuccess

    private val _responseToastMessage = MutableLiveData<Event<String>>()
    val responseToastMessage: LiveData<Event<String>>
        get() = _responseToastMessage

    fun addUser() {
        viewModelScope.launch {
            _preUploadCompleted.value = false
            if (validateNickname() && !repository.checkDuplicateUserName(nickname.value.toString())) {
                _addUserResult.value = repository.addUser(nickname.value.toString())
            }
            else if(repository.checkDuplicateUserName(nickname.value.toString())) {
                _addUserResult.value = false
                _preUploadCompleted.value = true
                _responseToastMessage.value = Event(NICKNAME_DUPLICATE)
            }
            else {
                _addUserResult.value = false
                _preUploadCompleted.value = true
                _responseToastMessage.value = Event(NICKNAME_ERROR)
            }
        }
    }

    fun validateNickname(): Boolean {
        val value: String = nickname.value ?: ""

        return when {
            value.isEmpty() -> {
                _nicknameErrorMessage.value = NICKNAME_REQUEST
                false
            }
            !value.matches(Constants.NICKNAME_PATTERN.toRegex()) -> {
                _nicknameErrorMessage.value = NICKNAME_ERROR
                false
            }
            else -> {
                _nicknameErrorMessage.value = NICKNAME_VALID
                true
            }
        }
    }

    fun handlePostResponse(responseBoolean: Boolean) {
        if (responseBoolean) {
            _uploadSuccess.value = Event(true)
            _responseToastMessage.value = Event(SUCCESS)
        } else {
            _uploadSuccess.value = Event(false)
        }
        _preUploadCompleted.value = true
    }

    companion object {
        fun provideFactory(repository: UserInfoRepository) = viewModelFactory {
            initializer {
                SettingNicknameViewModel(repository)
            }
        }
    }
}
