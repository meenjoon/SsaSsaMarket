package com.mbj.ssassamarket.ui.bindings

import androidx.appcompat.widget.SearchView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData

@BindingAdapter("searchText")
fun setSearchText(searchView: SearchView, text: MutableLiveData<String>) {
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            text.value = newText
            return true
        }
    })
}
