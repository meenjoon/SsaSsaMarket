package com.mbj.ssassamarket.util

import android.content.Context
import com.mbj.ssassamarket.R
import javax.inject.Inject

class Colors @Inject constructor(private val context: Context) {
    val randomColor: String
        get() = context.resources.getStringArray(R.array.random_colors).random()
}
