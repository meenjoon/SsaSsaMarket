package com.mbj.ssassamarket.ui.home

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.viewModels
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
import com.mbj.ssassamarket.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeProductFragment : BaseFragment(), ProductClickListener {

    override val binding get() = _binding as FragmentHomeProductBinding
    override val layoutId: Int get() = R.layout.fragment_home_product

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setupViews()
        setAdapter()
        observeError()
    }

    private fun setupViews() {
        setupSwipeRefreshLayout()
        setupSpinner()
        observeSearchText()
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

    private fun observeSearchText() {
        viewModel.searchText.observe(viewLifecycleOwner) {
            viewModel.updateSearchText()
        }
    }

    private fun setAdapter() {
        val category = arguments?.getSerializable(KEY_HOME_PRODUCT) as? Category
        if (category != null) {
            val adapter = HomeAdapter(this)
            binding.homeProductRv.adapter = adapter
            viewModel.updateCategory(category)
            viewModel.items.observe(viewLifecycleOwner, EventObserver { productList ->
                adapter.submitList(productList)
            })
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

    private fun observeError() {
        viewModel.isError.observe(viewLifecycleOwner, EventObserver { isError ->
            if (isError) {
                showToast(R.string.error_message_retry)
            }
        })
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(category: Category) = HomeProductFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_HOME_PRODUCT, category)
            }
        }
    }
}
