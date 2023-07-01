package com.mbj.ssassamarket.ui.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.RoundedCornersTransformation
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

@BindingAdapter("imageFirstUrl")
fun ImageView.loadFirstImage(imageLocations: List<String?>?) {
    val firstImageLocation = imageLocations?.firstOrNull()

    if (firstImageLocation != null) {
        val roundedCorners = RoundedCornersTransformation(15f)
        this.load(firstImageLocation.toString()) {
            transformations(roundedCorners)
            error(R.drawable.null_icon)
        }
    } else {
        this.load(R.drawable.null_icon)
    }
}
