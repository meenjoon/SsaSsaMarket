package com.mbj.ssassamarket.ui.bindings

import android.net.Uri
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
            crossfade(true)
            crossfade(1000)
            transformations(roundedCorners)
            error(R.drawable.null_icon)
        }
    } else {
        this.load(R.drawable.null_icon)
    }
}

@BindingAdapter("imageUrl")
fun ImageView.loadImage(imageUrl: String) {
    this.load(imageUrl) {
        transformations(RoundedCornersTransformation(15f))
        error(R.drawable.null_icon)
    }
}

@BindingAdapter("imageUriWithRoundedCorners")
fun ImageView.loadImageWithRoundedCorners(uri: Uri?) {
    uri?.let {
        this.load(it) {
            transformations(RoundedCornersTransformation(15f))
        }
    }
}
