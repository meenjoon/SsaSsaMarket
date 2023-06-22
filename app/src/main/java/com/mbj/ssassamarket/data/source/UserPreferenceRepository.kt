package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.SsaSsaMarketApplication
import com.mbj.ssassamarket.util.Constants.AUTO_LOGIN

class UserPreferenceRepository {

    fun saveAutoLoginState(autoLoginState: Boolean) {
        with(SsaSsaMarketApplication.preferenceManager) {
            putBoolean(AUTO_LOGIN, autoLoginState)
        }
    }

    fun getSaveAutoLoginState(): Boolean {
        return with(SsaSsaMarketApplication.preferenceManager) {
            getBoolean(AUTO_LOGIN, false)
        }
    }
}
