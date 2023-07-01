package com.mbj.ssassamarket.ui.home

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.data.model.FilterType
import com.mbj.ssassamarket.databinding.FragmentHomeProductBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.Constants.KEY_HOME_PRODUCT
import com.mbj.ssassamarket.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeProductFragment : BaseFragment() {

    override val binding get() = _binding as FragmentHomeProductBinding
    override val layoutId: Int get() = R.layout.fragment_home_product

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setAdapter()
        setupSpinner()
    }

    private fun setAdapter() {
        val category = arguments?.getSerializable(KEY_HOME_PRODUCT) as? Category
        if (category != null) {
            val adapter = HomeAdapter()
            binding.homeProductRv.adapter = adapter
            viewModel.updateCategory(category)
            viewModel.items.observe(viewLifecycleOwner, EventObserver { productList ->
                adapter.submitList(productList)
            })
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

            override fun onNothingSelected(parent: AdapterView<*>?) {
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
