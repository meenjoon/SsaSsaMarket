package com.mbj.ssassamarket.ui.bindings

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.firebase.storage.FirebaseStorage
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
    val storageRef = FirebaseStorage.getInstance().reference
    if (firstImageLocation != null) {
        val imageRef = storageRef.child(firstImageLocation)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            val roundedCorners = RoundedCornersTransformation(15f)
            this.load(uri.toString()) {
                transformations(roundedCorners)
                error(R.drawable.null_icon)
            }
        }.addOnFailureListener { exception ->
            Log.e("HomeAdapter", "Failed to load image: ${exception.message}")
        }
    } else {
        this.load(R.drawable.null_icon)
    }
}
