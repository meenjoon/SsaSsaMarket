package com.mbj.ssassamarket.ui.settingnickname

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import com.mbj.ssassamarket.util.Constants
import com.mbj.ssassamarket.util.Constants.NICKNAME_DUPLICATE
import com.mbj.ssassamarket.util.Constants.NICKNAME_ERROR
import com.mbj.ssassamarket.util.Constants.NICKNAME_REQUEST
import com.mbj.ssassamarket.util.Constants.NICKNAME_VALID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingNicknameViewModel @Inject constructor(private val repository: UserInfoRepository) :
    ViewModel() {

    val nickname = MutableStateFlow("")

    private val _nicknameErrorMessage = MutableStateFlow("")
    val nicknameErrorMessage: StateFlow<String> = _nicknameErrorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    private val _responseToastMessage = MutableStateFlow("")
    val responseToastMessage: StateFlow<String> = _responseToastMessage

    private val _addUserClicks = MutableSharedFlow<Unit>()

    init {
        viewModelScope.launch {
            _addUserClicks
                .conflate()
                .collectLatest {
                    if (_isLoading.value) {
                        return@collectLatest
                    }

                    if (nickname.value.isNullOrEmpty()) {
                        _responseToastMessage.value = NICKNAME_REQUEST
                    } else if (!nickname.value.matches(Constants.NICKNAME_PATTERN.toRegex())) {
                        _responseToastMessage.value = NICKNAME_ERROR
                    } else if (isNicknameDuplicate()) {
                        _responseToastMessage.value = NICKNAME_DUPLICATE
                    } else {
                        _isLoading.value = true
                        repository.addUser(
                            onComplete = { _isLoading.value = false },
                            onError = { _isError.value = true },
                            nickname.value
                        ).collect { response ->
                            if (response is ApiResultSuccess) {
                                _isCompleted.value = true
                            }
                        }
                    }
                }
        }
    }


    fun addUser() {
        viewModelScope.launch {
            _addUserClicks.emit(Unit)
        }
    }

    fun validateNickname() {
        viewModelScope.launch {
            nickname.debounce(300)
                .collectLatest { debouncedNickname ->
                    if (debouncedNickname.isEmpty()) {
                        _nicknameErrorMessage.value = NICKNAME_REQUEST
                    } else if (!debouncedNickname.matches(Constants.NICKNAME_PATTERN.toRegex())) {
                        _nicknameErrorMessage.value = NICKNAME_ERROR
                    } else if (isNicknameDuplicate()) {
                        _nicknameErrorMessage.value = NICKNAME_DUPLICATE
                    } else {
                        _nicknameErrorMessage.value = NICKNAME_VALID
                    }
                }
        }
    }

    private suspend fun isNicknameDuplicate(): Boolean {
        var isDuplicate = false

        repository.getUser(
            onComplete = { _isLoading.value = false },
            onError = { _isError.value = true }
        ).collect { response ->
            if (response is ApiResultSuccess) {
                val users = response.data
                isDuplicate = users.values.flatMap { it.values }.any { userInfo ->
                    userInfo.userName == nickname.value
                }
            }
        }
        return isDuplicate
    }
}
