package com.mbj.ssassamarket.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mbj.ssassamarket.SsaSsaMarketApplication
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.UserPreferenceRepository
import com.mbj.ssassamarket.util.Constants.AUTO_LOGIN
import com.mbj.ssassamarket.util.Event
import kotlinx.coroutines.launch

class LogInViewModel(private val userInfoRepository: UserInfoRepository, val userPreferenceRepository: UserPreferenceRepository) : ViewModel() {

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

    companion object {
        fun provideFactory(repository: UserInfoRepository, userPreferenceRepository: UserPreferenceRepository) = viewModelFactory {
            initializer {
                LogInViewModel(repository, userPreferenceRepository)
            }
        }
    }
}
