package com.mbj.ssassamarket

import android.app.Application
import com.mbj.ssassamarket.util.AppContainer
import com.mbj.ssassamarket.util.PreferenceManager

class SsaSsaMarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
        appContainer = AppContainer()
    }

    companion object {
        lateinit var preferenceManager: PreferenceManager
        lateinit var appContainer: AppContainer
    }
}
