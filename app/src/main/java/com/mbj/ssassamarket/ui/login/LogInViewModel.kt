package com.mbj.ssassamarket.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mbj.ssassamarket.SsaSsaMarketApplication
import com.mbj.ssassamarket.util.Constants.AUTO_LOGIN

class LogInViewModel() : ViewModel() {
    var autoLoginEnabled = MutableLiveData<Boolean>(SsaSsaMarketApplication.preferenceManager.getBoolean(AUTO_LOGIN, false))
}
