package com.mbj.ssassamarket.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.databinding.FragmentHomeProductBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.Constants.KEY_HOME_PRODUCT
import com.mbj.ssassamarket.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeProductFragment : BaseFragment() {

    override val binding get() = _binding as FragmentHomeProductBinding
    override val layoutId: Int get() = R.layout.fragment_home_product

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    private fun setAdapter() {
        val category = arguments?.getSerializable(KEY_HOME_PRODUCT) as? Category
        if (category != null) {
            val adapter = HomeAdapter()
            binding.homeProductRv.adapter = adapter
            viewModel.loadProductByCategory(category)
            viewModel.items.observe(viewLifecycleOwner, EventObserver { productList ->
              adapter.submitList(productList)
            })
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
