package com.mbj.ssassamarket.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.UserPreferenceRepository
import com.mbj.ssassamarket.data.source.remote.network.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: UserInfoRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    val isAccountExistsOnServer: StateFlow<Boolean?> = checkCurrentUserExists().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    val autoLoginState = userPreferenceRepository.getSaveAutoLoginState()
    var currentUser: FirebaseUser? = null

    init {
        viewModelScope.launch {
            currentUser = repository.getUserAndIdToken().first
        }
    }

    private fun checkCurrentUserExists(): Flow<Boolean> = repository.getUser(
        onComplete = {},
        onError = { _isError.value = true }
    ).mapNotNull { response ->
        if (response is ApiResultSuccess) {
            val userMap = response.data
            isAccountExistsOnServer(userMap)
        } else {
            null
        }
    }

    private suspend fun isAccountExistsOnServer(userMap: Map<String, Map<String, User>>): Boolean {
        val uid = repository.getUserAndIdToken().first?.uid ?: ""
        return userMap.containsKey(uid)
    }
}
