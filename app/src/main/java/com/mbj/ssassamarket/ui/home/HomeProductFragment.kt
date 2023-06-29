package com.mbj.ssassamarket.ui.home

import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentHomeProductBinding
import com.mbj.ssassamarket.ui.BaseFragment

class HomeProductFragment : BaseFragment() {

    override val binding get() = _binding as FragmentHomeProductBinding
    override val layoutId: Int get() = R.layout.fragment_home_product
}
