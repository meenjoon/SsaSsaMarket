package com.mbj.ssassamarket.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.UserPreferenceRepository
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(private val userInfoRepository: UserInfoRepository, val userPreferenceRepository: UserPreferenceRepository) : ViewModel() {

    var autoLoginEnabled = MutableLiveData<Boolean>(userPreferenceRepository.getSaveAutoLoginState())

    private val _preUploadCompleted = MutableLiveData<Boolean>()
    val preUploadCompleted: LiveData<Boolean>
        get() = _preUploadCompleted

    private val _addUserResult = MutableLiveData<Boolean>()
    val addUserResult: LiveData<Boolean>
        get() = _addUserResult

    private val _uploadSuccess = MutableLiveData<Event<Boolean>>()
    val uploadSuccess: LiveData<Event<Boolean>>
        get() = _uploadSuccess

    fun currentUserExists() {
        viewModelScope.launch {
            _preUploadCompleted.value = false
            _addUserResult.value = userInfoRepository.currentUserExists()
        }
    }

    fun handleGetResponse(responseBoolean: Boolean) {
        if (responseBoolean) {
            _uploadSuccess.value = Event(true)
        } else {
            _uploadSuccess.value = Event(false)
        }
        _preUploadCompleted.value = true
    }
}
