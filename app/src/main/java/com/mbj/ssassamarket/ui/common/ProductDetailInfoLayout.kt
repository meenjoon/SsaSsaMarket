package com.mbj.ssassamarket.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.ViewProductDetailInfoBinding
import com.mbj.ssassamarket.util.DateFormat.getFormattedElapsedTime


class ProductDetailInfoLayout (context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding: ViewProductDetailInfoBinding

    init {
        binding = ViewProductDetailInfoBinding.inflate(LayoutInflater.from(context), this)
        context.theme.obtainStyledAttributes(attrs, R.styleable.ProductDetailInfoLayout, 0, 0)
            .apply {
                try {
                    val titleText = getString(R.styleable.ProductDetailInfoLayout_detailTitleText)
                    val titleEnabled = getBoolean(R.styleable.ProductDetailInfoLayout_detailTitleEnabled, true)
                    val priceText = getString(R.styleable.ProductDetailInfoLayout_detailPriceText)
                    val priceEnabled = getBoolean(R.styleable.ProductDetailInfoLayout_detailPriceEnabled, true)
                    val contentText = getString(R.styleable.ProductDetailInfoLayout_detailContentText)
                    val contentEnabled = getBoolean(R.styleable.ProductDetailInfoLayout_detailContentEnabled, true)
                    val timeTextColor = getColor(R.styleable.ProductDetailInfoLayout_detailTimeTextColor, 0)
                    val nicknameText = getString(R.styleable.ProductDetailInfoLayout_detailNicknameText)
                    val timeText = getString(R.styleable.ProductDetailInfoLayout_detailTimeText)
                    val locationText = getString(R.styleable.ProductDetailInfoLayout_detailLocationText)
                    val titleTextColor = getColor(R.styleable.ProductDetailInfoLayout_detailTimeTextColor, 0)
                    val priceTextColor = getColor(R.styleable.ProductDetailInfoLayout_detailTimeTextColor, 0)
                    val contentTextColor = getColor(R.styleable.ProductDetailInfoLayout_detailTimeTextColor, 0)

                    setDetailTitleText(titleText)
                    setDetailTitleEnabled(titleEnabled)
                    setDetailPriceText(priceText)
                    setDetailPriceEnabled(priceEnabled)
                    setDetailContentText(contentText)
                    setDetailContentEnabled(contentEnabled)
                    setDetailTimeTextColor(timeTextColor)
                    setDetailNicknameText(nicknameText)
                    setDetailTimeText(timeText)
                    setDetailLocationText(locationText)
                    setDetailTitleTextColor(titleTextColor)
                    setDetailPriceTextColor(priceTextColor)
                    setDetailContentTextColor(contentTextColor)
                } finally {
                    recycle()
                }
            }
    }

    fun setDetailTitleText(text: CharSequence?) {
        binding.detailTitleTiev.setText(text)
    }

    fun setDetailTitleEnabled(enabled: Boolean) {
        binding.detailTitleTiev.isEnabled = enabled
    }

    fun setDetailPriceText(text: CharSequence?) {
        binding.detailPriceTiev.setText(text)
    }

    fun setDetailPriceEnabled(enabled: Boolean) {
        binding.detailPriceTiev.isEnabled = enabled
    }

    fun setDetailContentText(text: CharSequence?) {
        binding.detailContentTiev.setText(text)
    }

    fun setDetailContentEnabled(enabled: Boolean) {
        binding.detailContentTiev.isEnabled = enabled
    }

    fun setDetailTimeTextColor(color: Int) {
        if (color != 0) {
            binding.detailTimeTv.setTextColor(color)
        }
    }

    fun setDetailNicknameText(text: CharSequence?) {
        binding.detailNicknameTv.setText(text)
    }

    fun setDetailTimeText(text: CharSequence?) {
        val formattedText = text?.toString()?.let { getFormattedElapsedTime(it) }
        binding.detailTimeTv.text = formattedText
    }

    fun setDetailLocationText(text: CharSequence?) {
        binding.detailLocationTv.setText(text)
    }

    fun setDetailTitleTextColor(colorResourceId: Int) {
        if (colorResourceId != 0) {
            binding.detailTitleTiev.setTextColor(colorResourceId)
        }
    }

    fun setDetailPriceTextColor(colorResourceId: Int) {
        if (colorResourceId != 0) {
            binding.detailPriceTiev.setTextColor(colorResourceId)
        }
    }

    fun setDetailContentTextColor(colorResourceId: Int) {
        if (colorResourceId != 0) {
            binding.detailContentTiev.setTextColor(colorResourceId)
        }
    }

    fun getDetailTitleText(): String {
        return binding.detailTitleTiev.text.toString()
    }

    fun getDetailPriceText(): String {
        return binding.detailPriceTiev.text.toString()
    }

    fun getDetailContentText(): String {
        return binding.detailContentTiev.text.toString()
    }
}
