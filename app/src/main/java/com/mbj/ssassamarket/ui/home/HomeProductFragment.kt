package com.mbj.ssassamarket.ui.home

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.data.model.FilterType
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.data.model.UserType
import com.mbj.ssassamarket.databinding.FragmentHomeProductBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.common.ProductClickListener
import com.mbj.ssassamarket.util.Constants.KEY_HOME_PRODUCT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeProductFragment : BaseFragment(), ProductClickListener {

    override val binding get() = _binding as FragmentHomeProductBinding
    override val layoutId: Int get() = R.layout.fragment_home_product

    private lateinit var adapter: HomeAdapter
    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setupViews()
        setAdapter()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchText.collectLatest { searchText ->
                        viewModel.updateSearchText()
                    }
                }
                launch {
                    viewModel.items.collectLatest { productList ->
                        adapter.submitList(productList)
                    }
                }
            }
        }
    }

    private fun setupViews() {
        setupSwipeRefreshLayout()
        setupSpinner()
    }

    private fun setupSwipeRefreshLayout() {
        binding.homeSrl.setOnRefreshListener {
            viewModel.refreshData()
            binding.homeSrl.isRefreshing = false
        }
    }

    private fun setupSpinner() {
        binding.homeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val filter = when (position) {
                    0 -> FilterType.LATEST
                    1 -> FilterType.PRICE
                    else -> FilterType.FAVORITE
                }
                viewModel.updateFilterType(filter)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setAdapter() {
        val category = arguments?.getSerializable(KEY_HOME_PRODUCT) as? Category
        if (category != null) {
            adapter = HomeAdapter(this)
            binding.homeProductRv.adapter = adapter
            viewModel.updateCategory(category)
        }
    }

    override fun onProductClick(productPostItem: Pair<String, ProductPostItem>) {
        viewModel.navigateBasedOnUserType(productPostItem.second.id) { userType ->
            when (userType) {
                UserType.SELLER -> {
                    val action = HomeFragmentDirections.actionNavigationHomeToSellerFragment(
                        productPostItem.first,
                        productPostItem.second
                    )
                    findNavController().navigate(action)
                }
                UserType.BUYER -> {
                    val action = HomeFragmentDirections.actionNavigationHomeToBuyerFragment(
                        productPostItem.first,
                        productPostItem.second
                    )
                    findNavController().navigate(action)
                }
            }
        }
    }

    companion object {
        fun newInstance(category: Category) = HomeProductFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_HOME_PRODUCT, category)
            }
        }
    }
}
