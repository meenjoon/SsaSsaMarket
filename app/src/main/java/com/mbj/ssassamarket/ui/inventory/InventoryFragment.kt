package com.mbj.ssassamarket.ui.inventory

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.UserType
import com.mbj.ssassamarket.databinding.FragmentInventoryBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.common.ProductClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryFragment : BaseFragment(), ProductClickListener {
    override val binding get() = _binding as FragmentInventoryBinding
    override val layoutId: Int get() = R.layout.fragment_inventory

    private val viewModel: InventoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = InventoryOuterAdapter(this)
        binding.viewModel = viewModel
        binding.inventoryOuterRv.adapter = adapter
        viewModel.getMyNickname()
        viewModel.initProductPostItemList()

        observeInventoryDataList(adapter)
    }

    private fun observeInventoryDataList(adapter: InventoryOuterAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.inventoryDataList.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collectLatest { inventoryDataList ->
                adapter.submitList(inventoryDataList)
            }
        }
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
