package com.mbj.ssassamarket.ui.bindings

import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.util.Constants
import kotlinx.coroutines.flow.StateFlow

@BindingAdapter("nicknameValidColor")
fun Button.setNicknameValidColor(errorText: StateFlow<String>) {
    val color = when (errorText.value) {
        Constants.NICKNAME_REQUEST -> ContextCompat.getColor(context, R.color.orange_100)
        Constants.NICKNAME_ERROR -> ContextCompat.getColor(context, R.color.orange_100)
        Constants.NICKNAME_DUPLICATE -> ContextCompat.getColor(context, R.color.orange_100)
        Constants.NICKNAME_VALID -> ContextCompat.getColor(context, R.color.orange_700)
        else -> null
    }
    if (color != null) {
        this.setBackgroundColor(color)
    }
}
