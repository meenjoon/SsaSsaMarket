package com.mbj.ssassamarket.ui.launcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.mbj.ssassamarket.SsaSsaMarketApplication
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.util.Constants.AUTO_LOGIN
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: UserInfoRepository) : ViewModel() {

    private val _getUserResult = MutableLiveData<Boolean>()
    val getUserResult: LiveData<Boolean>
        get() = _getUserResult

    val autoLoginState = SsaSsaMarketApplication.preferenceManager.getBoolean(AUTO_LOGIN, false)
    val currentUser = FirebaseAuth.getInstance().currentUser

    fun checkCurrentUserExists() {
        viewModelScope.launch {
            _getUserResult.value = repository.currentUserExists()
        }
    }

    companion object {
        fun provideFactory(repository: UserInfoRepository) = viewModelFactory {
            initializer {
                SplashViewModel(repository)
            }
        }
    }
}
