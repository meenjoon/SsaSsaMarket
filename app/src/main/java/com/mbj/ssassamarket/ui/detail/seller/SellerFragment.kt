package com.mbj.ssassamarket.ui.detail.seller

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.EditMode
import com.mbj.ssassamarket.databinding.FragmentSellerBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.detail.BannerAdapter
import com.mbj.ssassamarket.util.Constants
import com.mbj.ssassamarket.util.EventObserver
import com.mbj.ssassamarket.util.ProgressDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSellerBinding
    override val layoutId: Int get() = R.layout.fragment_seller

    private val args: SellerFragmentArgs by navArgs()
    private val viewModel: SellerViewModel by viewModels()

    private var progressDialog: ProgressDialogFragment? = null
    private lateinit var bannerAdapter: BannerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        initViewModel()
        observeData()
        setupViewPager()
    }

    private fun setupUI() {
        binding.detailBackIv.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initViewModel() {
        viewModel.setProduct(args.product)
        viewModel.setEditMode(EditMode.READ_ONLY)
    }

    private fun observeData() {
        viewModel.editMode.observe(viewLifecycleOwner, EventObserver { editMode ->
            when (editMode) {
                EditMode.READ_ONLY -> setReadOnly()
                EditMode.EDIT -> {
                    TODO()
                }
            }
        })

        viewModel.getProductNickname()

        viewModel.nickname.observe(viewLifecycleOwner, EventObserver { nickname ->
            binding.detailReceiver.setDetailNicknameText(nickname)
            viewModel.handlePostResponse(nickname)
        })

        viewModel.productNicknameCompleted.observe(viewLifecycleOwner) { productNicknameCompleted ->
            if (!productNicknameCompleted) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        }

        viewModel.productNicknameSuccess.observe(viewLifecycleOwner, EventObserver { productNicknameSuccess ->
                if (!productNicknameSuccess) {
                    showToast(R.string.nickname_response_failure)
                }
            })
    }

    private fun setupViewPager() {
        bannerAdapter = BannerAdapter()
        binding.detailVp2.adapter = bannerAdapter

        val pageWidth = resources.getDimension(R.dimen.viewpager_item_width)
        val pageMargin = resources.getDimension(R.dimen.viewpager_item_margin)
        val screenWidth = resources.displayMetrics.widthPixels
        val offset = screenWidth - pageWidth - pageMargin

        binding.detailVp2.offscreenPageLimit = 2
        binding.detailVp2.setPageTransformer { page, position ->
            page.translationX = position * -offset
        }

        TabLayoutMediator(binding.detailTl, binding.detailVp2) { tab, position ->
        }.attach()
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialogFragment()
        progressDialog?.show(childFragmentManager, Constants.PROGRESS_DIALOG)
    }

    private fun dismissLoadingDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun setReadOnly() {
        val product = viewModel.product.value
        bannerAdapter.submitList(product?.imageLocations)
        binding.detailReceiver.apply {
            setDetailContentEnabled(false)
            setDetailPriceEnabled(false)
            setDetailTitleEnabled(false)
            setDetailTitleText(product?.title)
            setDetailTimeText(product?.createdDate)
            setDetailPriceText(product?.price.toString())
            setLocation(product?.location)
            setDetailContentText(product?.content)
        }
    }
}
