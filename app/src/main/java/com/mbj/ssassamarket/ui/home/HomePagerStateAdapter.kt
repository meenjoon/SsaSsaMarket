package com.mbj.ssassamarket.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mbj.ssassamarket.data.model.Category

class HomePagerStateAdapter(fragment: Fragment, private val categories: List<Category>): FragmentStateAdapter(fragment) {

    override fun getItemCount() = categories.size

    override fun createFragment(position: Int): Fragment {
        val category = categories.get(position)
        return HomeProductFragment.newInstance(category)
    }
}
