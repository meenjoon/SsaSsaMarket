package com.mbj.ssassamarket.data.source

import com.mbj.ssassamarket.util.Constants.AUTO_LOGIN
import com.mbj.ssassamarket.util.PreferenceManager
import javax.inject.Inject

class UserPreferenceRepository @Inject constructor(private val preferenceManager: PreferenceManager) {

    fun saveAutoLoginState(autoLoginState: Boolean) {
        preferenceManager.putBoolean(AUTO_LOGIN, autoLoginState)
    }

    fun getSaveAutoLoginState(): Boolean {
        return preferenceManager.getBoolean(AUTO_LOGIN, false)
    }
}
