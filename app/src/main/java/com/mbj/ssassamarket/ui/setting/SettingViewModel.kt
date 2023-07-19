package com.mbj.ssassamarket.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLogoutSuccess = MutableStateFlow(false)
    val isLogoutSuccess: StateFlow<Boolean> = _isLogoutSuccess

    private val _isLogoutError = MutableStateFlow(false)
    val isLogoutError: StateFlow<Boolean> = _isLogoutError

    fun onLogoutButtonClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            userInfoRepository.logout(
                onComplete = { _isLoading.value = false },
                onError = { _isLogoutError.value = true }
            ).collectLatest { response ->
                if (response is ApiResultSuccess) {
                    _isLogoutSuccess.value = true
                }
            }
        }
    }
}
