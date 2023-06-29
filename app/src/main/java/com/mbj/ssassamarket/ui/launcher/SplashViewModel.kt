package com.mbj.ssassamarket.ui.launcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.UserPreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: UserInfoRepository, private val userPreferenceRepository: UserPreferenceRepository) : ViewModel() {

    private val _getUserResult = MutableLiveData<Boolean>()
    val getUserResult: LiveData<Boolean>
        get() = _getUserResult

    val autoLoginState = userPreferenceRepository.getSaveAutoLoginState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    fun checkCurrentUserExists() {
        viewModelScope.launch {
            _getUserResult.value = repository.currentUserExists()
        }
    }
}
