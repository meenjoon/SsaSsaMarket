package com.mbj.ssassamarket.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.UserPreferenceRepository
import com.mbj.ssassamarket.data.source.remote.network.onError
import com.mbj.ssassamarket.data.source.remote.network.onSuccess
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(private val userInfoRepository: UserInfoRepository, val userPreferenceRepository: UserPreferenceRepository) : ViewModel() {

    var autoLoginEnabled = MutableLiveData<Boolean>(userPreferenceRepository.getSaveAutoLoginState())

    private val _isLoading = MutableLiveData(Event(false))
    val isLoading: LiveData<Event<Boolean>> get() =  _isLoading

    private val _isAccountExistsOnServer = MutableLiveData<Event<Boolean>>()
    val isAccountExistsOnServer: LiveData<Event<Boolean>> get() = _isAccountExistsOnServer

    private val _isError = MutableLiveData(Event(false))
    val isError: LiveData<Event<Boolean>> get() =  _isError

    fun currentUserExists() {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            val result = userInfoRepository.getUser()
            result.onSuccess { userMap ->
                _isLoading.value = Event(false)
                val isExists = isAccountExistsOnServer(userMap)
                _isAccountExistsOnServer.value = Event(isExists)
            }.onError { code, message ->
                _isLoading.value = Event(false)
                _isError.value = Event(true)
            }
        }
    }

    private suspend fun isAccountExistsOnServer(userMap: Map<String, Map<String, User>>): Boolean {
        val uId = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
        return userMap.containsKey(uId)
    }
}
