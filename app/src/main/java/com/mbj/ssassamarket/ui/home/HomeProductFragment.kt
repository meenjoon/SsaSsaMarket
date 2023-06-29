package com.mbj.ssassamarket.ui.home

import android.os.Bundle
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.databinding.FragmentHomeProductBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.Constants.KEY_HOME_PRODUCT

class HomeProductFragment : BaseFragment() {

    override val binding get() = _binding as FragmentHomeProductBinding
    override val layoutId: Int get() = R.layout.fragment_home_product

    companion object {
        fun newInstance(category: Category) = HomeProductFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_HOME_PRODUCT, category)
            }
        }
    }
}
