package com.mbj.ssassamarket.ui.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.mbj.ssassamarket.R

@BindingAdapter("symbolSrcCompat")
fun ImageView.setSymbolSrcCompat(isNullOrEmpty: Boolean) {
    val drawableResId = if (isNullOrEmpty) {
        R.drawable.currency_symbol_empty_icon
    } else {
        R.drawable.currency_symbol_full_icon
    }

    setImageResource(drawableResId)
}
