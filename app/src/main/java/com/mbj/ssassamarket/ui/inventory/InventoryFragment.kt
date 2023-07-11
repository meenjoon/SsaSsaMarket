package com.mbj.ssassamarket.ui.inventory

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentInventoryBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventoryFragment : BaseFragment() {
    override val binding get() = _binding as FragmentInventoryBinding
    override val layoutId: Int get() = R.layout.fragment_inventory

    private val viewModel: InventoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = InventoryOuterAdapter()
        binding.inventoryOuterRv.adapter = adapter

        viewModel.inventoryDataList.observe(viewLifecycleOwner, EventObserver{ inventoryDataList ->
            adapter.submitList(inventoryDataList)
        })

        viewModel.nickname.observe(viewLifecycleOwner, EventObserver{ nickname ->
            binding.inventoryNicknameTv.text = nickname
        })
    }
}
