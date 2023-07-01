package com.mbj.ssassamarket.ui.home


import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.databinding.FragmentHomeBinding
import com.mbj.ssassamarket.ui.BaseFragment

class HomeFragment : BaseFragment() {

    override val binding get() = _binding as FragmentHomeBinding
    override val layoutId: Int get() = R.layout.fragment_home

    private val homeCategories = Category.values().toList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeVp2.adapter = HomePagerStateAdapter(this, homeCategories)
        TabLayoutMediator(binding.homeTl, binding.homeVp2) { tab, position ->
            tab.text = homeCategories[position].label
        }.attach()
    }
}
