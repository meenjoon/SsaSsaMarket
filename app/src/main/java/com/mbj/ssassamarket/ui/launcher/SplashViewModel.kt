package com.mbj.ssassamarket.ui.launcher

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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
class SplashViewModel @Inject constructor(private val repository: UserInfoRepository, private val userPreferenceRepository: UserPreferenceRepository) : ViewModel() {

    private val _isAccountExistsOnServer = MutableLiveData<Event<Boolean>>()
    val isAccountExistsOnServer: LiveData<Event<Boolean>> get() = _isAccountExistsOnServer

    private val _isError = MutableLiveData(Event(false))
    val isError: LiveData<Event<Boolean>> = _isError

    val autoLoginState = userPreferenceRepository.getSaveAutoLoginState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    fun checkCurrentUserExists() {
        viewModelScope.launch {
            val result = repository.getUser()
            result.onSuccess { userMap ->
                val isExists = isAccountExistsOnServer(userMap)
                _isAccountExistsOnServer.value = Event(isExists)
            }.onError { code, message ->
                _isError.value = Event(true)
            }
        }
    }

    private suspend fun isAccountExistsOnServer(userMap: Map<String, Map<String, User>>): Boolean {
        val uId = repository.getUserAndIdToken().first?.uid ?: ""
        return userMap.containsKey(uId)
    }
}
