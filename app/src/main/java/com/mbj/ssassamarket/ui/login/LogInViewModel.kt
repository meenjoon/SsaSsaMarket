package com.mbj.ssassamarket.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.UserPreferenceRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(private val userInfoRepository: UserInfoRepository, val userPreferenceRepository: UserPreferenceRepository) : ViewModel() {

    var autoLoginEnabled = MutableStateFlow(userPreferenceRepository.getSaveAutoLoginState())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> =  _isLoading

    private val _isAccountExistsOnServer = MutableStateFlow<Boolean?>(null)
    val isAccountExistsOnServer: StateFlow<Boolean?> = _isAccountExistsOnServer

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> =  _isError

    fun currentUserExists() {
        viewModelScope.launch {
            _isLoading.value = (true)
            userInfoRepository.getUser(
                onComplete = { _isLoading.value = (false)},
                onError = { _isError.value = (true)}
            ).collect { response ->
                if (response is ApiResultSuccess) {
                    val userMap = response.data
                    val isExists = isAccountExistsOnServer(userMap)
                    _isAccountExistsOnServer.value = isExists
                }
            }
        }
    }

    private suspend fun isAccountExistsOnServer(userMap: Map<String, Map<String, User>>): Boolean {
        val uid = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
        return userMap.containsKey(uid)
    }
}
