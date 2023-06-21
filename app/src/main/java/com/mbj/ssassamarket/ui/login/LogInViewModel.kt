package com.mbj.ssassamarket.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mbj.ssassamarket.SsaSsaMarketApplication
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.util.Constants.AUTO_LOGIN

class LogInViewModel(private val userInfoRepository: UserInfoRepository) : ViewModel() {

    var autoLoginEnabled = MutableLiveData<Boolean>(SsaSsaMarketApplication.preferenceManager.getBoolean(AUTO_LOGIN, false))

    suspend fun currentUserExists(): Boolean {
        return userInfoRepository.currentUserExists()
    }

    companion object {
        fun provideFactory(repository: UserInfoRepository) = viewModelFactory {
            initializer {
                LogInViewModel(repository)
            }
        }
    }
}
