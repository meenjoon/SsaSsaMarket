package com.mbj.ssassamarket.ui.bindings

import android.view.View
import androidx.databinding.BindingAdapter
import com.mbj.ssassamarket.data.model.EditMode

@BindingAdapter("visible")
fun isVisible(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("app:visibleReadOnly")
fun setVisibleReadOnly(view: View, editMode: EditMode) {
    view.visibility = when (editMode) {
        EditMode.READ_ONLY -> View.VISIBLE
        EditMode.EDIT -> View.GONE
    }
}

@BindingAdapter("app:visibleEdit")
fun setVisibleEdit(view: View, editMode: EditMode) {
    view.visibility = when (editMode) {
        EditMode.READ_ONLY -> View.GONE
        EditMode.EDIT -> View.VISIBLE
    }
}

