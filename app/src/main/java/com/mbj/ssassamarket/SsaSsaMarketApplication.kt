package com.mbj.ssassamarket

import android.app.Application
import com.mbj.ssassamarket.data.source.local.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SsaSsaMarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appDatabase = AppDatabase.create(applicationContext)
    }

    companion object {
        lateinit var appDatabase: AppDatabase
    }
}
