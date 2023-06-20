package com.mbj.ssassamarket.util

import android.content.Context

class PreferenceManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        "com.mbj.ssassamarket.PREFERENCE_KEY",
        Context.MODE_PRIVATE
    )

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
