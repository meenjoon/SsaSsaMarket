package com.mbj.ssassamarket.ui.inventory

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.UserType
import com.mbj.ssassamarket.databinding.FragmentInventoryBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.common.ProductClickListener
import com.mbj.ssassamarket.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventoryFragment : BaseFragment(), ProductClickListener {
    override val binding get() = _binding as FragmentInventoryBinding
    override val layoutId: Int get() = R.layout.fragment_inventory

    private val viewModel: InventoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = InventoryOuterAdapter(this)
        binding.inventoryOuterRv.adapter = adapter
        viewModel.getNickname()
        viewModel.initProductPostItemList()

        viewModel.inventoryDataList.observe(viewLifecycleOwner, EventObserver{ inventoryDataList ->
            adapter.submitList(inventoryDataList)
        })

        viewModel.nickname.observe(viewLifecycleOwner, EventObserver{ nickname ->
            binding.inventoryNicknameTv.text = nickname
        })
    }

    override fun onProductClick(productPostItem: Pair<String, ProductPostItem>) {
        viewModel.navigateBasedOnUserType(productPostItem.second.id) { userType ->
            when (userType) {
                UserType.SELLER -> {
                    val action = InventoryFragmentDirections.actionNavigationInventoryToSellerFragment(productPostItem.first, productPostItem.second)
                    findNavController().navigate(action)
                }
                UserType.BUYER -> {
                    val action = InventoryFragmentDirections.actionNavigationInventoryToBuyerFragment(productPostItem.first, productPostItem.second)
                    findNavController().navigate(action)
                }
            }
        }
    }
}
