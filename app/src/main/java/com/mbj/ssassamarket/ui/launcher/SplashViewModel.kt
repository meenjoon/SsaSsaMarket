package com.mbj.ssassamarket.ui.launcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.UserPreferenceRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import com.mbj.ssassamarket.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: UserInfoRepository, private val userPreferenceRepository: UserPreferenceRepository) : ViewModel() {

    private val _isAccountExistsOnServer = MutableLiveData<Event<Boolean>>()
    val isAccountExistsOnServer: LiveData<Event<Boolean>> get() = _isAccountExistsOnServer

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    val autoLoginState = userPreferenceRepository.getSaveAutoLoginState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    fun checkCurrentUserExists() {
        viewModelScope.launch {
            repository.getUser(
                onComplete = {},
                onError = { _isError.value = (true) }
            ).collect { response ->
                if (response is ApiResultSuccess) {
                    val userMap = response.data
                    val isExists = isAccountExistsOnServer(userMap)
                    _isAccountExistsOnServer.value = Event(isExists)
                }
            }
        }
    }

    private suspend fun isAccountExistsOnServer(userMap: Map<String, Map<String, User>>): Boolean {
        val uId = repository.getUserAndIdToken().first?.uid ?: ""
        return userMap.containsKey(uId)
    }
}
