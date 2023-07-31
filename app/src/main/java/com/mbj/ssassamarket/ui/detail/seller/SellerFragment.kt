package com.mbj.ssassamarket.ui.detail.seller

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentSellerBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.detail.BannerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SellerFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSellerBinding
    override val layoutId: Int = R.layout.fragment_seller

    private val args: SellerFragmentArgs by navArgs()
    private val viewModel: SellerViewModel by viewModels()

    private lateinit var bannerAdapter: BannerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        initViewModel()
        setupViewPager()
        setupClickListeners()
        observeProductUpdates()
    }

    private fun initViewModel() {
        viewModel.initializeProduct(args.postId, args.product)
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

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun updateBannerAdapter() {
        val product = viewModel.product.value
        bannerAdapter.submitList(product?.imageLocations)
    }

    private fun setupClickListeners() {
        binding.detailEditBt.setOnClickListener {
            showEditModeExitConfirmationDialog()
        }
        binding.detailSubmitTv.setOnClickListener {
            viewModel.updateProduct(
                binding.detailReceiver.getDetailTitleText(),
                binding.detailReceiver.getDetailPriceText(),
                binding.detailReceiver.getDetailContentText()
            )
        }
        binding.detailBackIv.setOnClickListener {
            if (viewModel.isReadOnlyMode()) {
                findNavController().navigateUp()
            } else {
                showEditModeExitConfirmationDialog()
            }
        }
        binding.detailProductDeletionIv.setOnClickListener {
            showProductDeleteConfirmationDialog()
        }
    }

    private fun showEditModeExitConfirmationDialog() {
        val dialogMessage = if (viewModel.isReadOnlyMode().not()) {
            getString(R.string.confirmation_dialog_message_read)
        } else {
            getString(R.string.confirmation_dialog_message_edit)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirmation_dialog_title)
            .setMessage(dialogMessage)
            .setPositiveButton(R.string.permission_positive) { _, _ ->
                viewModel.toggleEditMode()
            }
            .setNegativeButton(R.string.permission_negative) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showProductDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirmation_dialog_title)
            .setMessage(R.string.confirmation_dialog_message_delete)
            .setPositiveButton(R.string.permission_positive) { _, _ ->
                viewModel.deleteProductData()
            }
            .setNegativeButton(R.string.permission_negative) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeProductUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.productUpdateCompleted.collectLatest { productUpdateCompleted ->
                        if (productUpdateCompleted) {
                            findNavController().navigateUp()
                            showToast(R.string.product_update_success)
                        }
                    }
                }
                launch {
                    viewModel.product.collectLatest { product ->
                        updateBannerAdapter()
                    }
                }
                launch {
                    viewModel.isProductInfoUnchanged.collectLatest { isProductInfoUnchanged ->
                        if (isProductInfoUnchanged) {
                            showToast(R.string.request_edit_product)
                        }
                    }
                }
                launch {
                    viewModel.productDeleteSuccess.collectLatest {productDeleteSuccess ->
                        if (productDeleteSuccess) {
                            findNavController().navigateUp()
                            showToast(R.string.product_delete_success)
                        }
                    }
                }
            }
        }
    }
}
