package com.mbj.ssassamarket.util

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.mbj.ssassamarket.databinding.ProgressIndicatorBinding

class ProgressIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes){

    init {
        ProgressIndicatorBinding.inflate(LayoutInflater.from(context), this)
    }
}
