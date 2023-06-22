package com.mbj.ssassamarket.ui.home


import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentHomeBinding
import com.mbj.ssassamarket.ui.BaseFragment

class HomeFragment : BaseFragment() {

    override val binding get() = _binding as FragmentHomeBinding
    override val layoutId: Int get() = R.layout.fragment_home
}
