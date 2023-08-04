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
    private val userInfoRepository: UserInfoRepository,
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

    private val _isFcmTokenRefreshCompleted = MutableStateFlow<Boolean>(false)
    val isFcmTokenRefreshCompleted: StateFlow<Boolean> = _isFcmTokenRefreshCompleted

    private var myUid: String? = null
    private var userPostKey: String? = null

    init {
        viewModelScope.launch {
            currentUser = userInfoRepository.getUserAndIdToken().first
        }
    }

    private fun checkCurrentUserExists(): Flow<Boolean> = userInfoRepository.getUser(
        onComplete = { },
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
        myUid = userInfoRepository.getUserAndIdToken().first?.uid ?: ""
        return userMap.containsKey(myUid)
    }

    fun refreshFcmToken() {
        viewModelScope.launch {
            userInfoRepository.getUser(
                onComplete = { },
                onError = { _isError.value = true }
            ).collectLatest { response ->
                if (response is ApiResultSuccess) {
                    response.data.forEach { (uid, userData) ->
                        if (uid == myUid) {
                            userPostKey = userData.keys.firstOrNull()
                            updateFcmToken(userPostKey!!)
                        }
                    }
                }
            }
        }
    }

    private suspend fun updateFcmToken(userPostKey: String) {
        if (myUid != null) {
            userInfoRepository.updateUserFcmToken(
                onComplete = { },
                onError = { _isError.value = (true) },
                myUid!!,
                userPostKey
            ).collectLatest { response ->
                if (response is ApiResultSuccess) {
                    _isFcmTokenRefreshCompleted.value = true
                }
            }
        }
    }
}
