package com.mbj.ssassamarket.ui.inventory

import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentInventoryBinding
import com.mbj.ssassamarket.ui.BaseFragment

class InventoryFragment : BaseFragment() {
    override val binding get() = _binding as FragmentInventoryBinding
    override val layoutId: Int get() = R.layout.fragment_inventory
}
