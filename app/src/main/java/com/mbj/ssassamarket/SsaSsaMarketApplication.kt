package com.mbj.ssassamarket

import android.app.Application
import com.mbj.ssassamarket.util.PreferenceManager

class SsaSsaMarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
    }

    companion object {
        lateinit var preferenceManager: PreferenceManager
    }
}
