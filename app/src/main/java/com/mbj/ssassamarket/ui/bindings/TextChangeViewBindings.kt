package com.mbj.ssassamarket.ui.bindings

import androidx.appcompat.widget.SearchView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.util.Constants.NICKNAME_DUPLICATE
import com.mbj.ssassamarket.util.Constants.NICKNAME_ERROR
import com.mbj.ssassamarket.util.Constants.NICKNAME_REQUEST
import com.mbj.ssassamarket.util.Constants.NICKNAME_VALID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@BindingAdapter("searchText")
fun SearchView.setSearchText(text: StateFlow<String>) {
    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            (text as? MutableStateFlow)?.tryEmit(newText.orEmpty())
            return true
        }
    })
}

@BindingAdapter("app:nicknameErrorText")
fun TextInputLayout.setNicknameErrorText(errorText: StateFlow<String>) {
    this.error = when (errorText.value) {
        NICKNAME_REQUEST -> context.getString(R.string.setting_nickname_request_nickname)
        NICKNAME_ERROR -> context.getString(R.string.setting_nickname_error_nickname)
        NICKNAME_DUPLICATE -> context.getString(R.string.setting_nickname_duplicate)
        NICKNAME_VALID -> null
        else -> null
    }
}
