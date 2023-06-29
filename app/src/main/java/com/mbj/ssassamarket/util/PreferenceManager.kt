package com.mbj.ssassamarket.util

import android.content.SharedPreferences
import javax.inject.Inject

class PreferenceManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun putBoolean(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defValue) ?: defValue
    }
}
