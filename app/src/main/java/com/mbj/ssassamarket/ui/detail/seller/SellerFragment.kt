package com.mbj.ssassamarket.ui.detail.seller

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.EditMode
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.databinding.FragmentSellerBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.detail.BannerAdapter
import com.mbj.ssassamarket.util.Constants
import com.mbj.ssassamarket.util.Constants.CONTENT
import com.mbj.ssassamarket.util.Constants.PRICE
import com.mbj.ssassamarket.util.Constants.TITLE
import com.mbj.ssassamarket.util.EventObserver
import com.mbj.ssassamarket.util.ProgressDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSellerBinding
    override val layoutId: Int = R.layout.fragment_seller

    private val args: SellerFragmentArgs by navArgs()
    private val viewModel: SellerViewModel by viewModels()

    private var progressDialog: ProgressDialogFragment? = null
    private lateinit var bannerAdapter: BannerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        initViewModel()
        observeData()
        setupViewPager()
        setupClickListeners()
        setupTextChangeListeners()
    }

    private fun initViewModel() {
        viewModel.initializeProduct( args.postId, args.product)
        viewModel.getProductNickname()
    }

    private fun observeData() {
        observeEditMode()
        observeNicknameResponse()
        observeProductData()
        observeProductUpdate()
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

    private fun setEditMode(editMode: EditMode) {
        val product = viewModel.product.value
        bannerAdapter.submitList(product?.peekContent()?.imageLocations)
        binding.detailEditAblTv.visibility =
            if (editMode == EditMode.EDIT) View.VISIBLE else View.INVISIBLE
        binding.detailSubmitTv.visibility =
            if (editMode == EditMode.EDIT) View.VISIBLE else View.INVISIBLE
        binding.detailEditBt.visibility =
            if(editMode == EditMode.READ_ONLY) View.VISIBLE else View.INVISIBLE

        binding.detailReceiver.apply {
            val isEditMode = editMode == EditMode.EDIT
            setDetailContentEnabled(isEditMode)
            setDetailPriceEnabled(isEditMode)
            setDetailTitleEnabled(isEditMode)
            setDetailTitleText(product?.peekContent()?.title)
            setDetailTimeText(product?.peekContent()?.createdDate)
            setDetailPriceText(product?.peekContent()?.price.toString())
            setDetailLocationText(product?.peekContent()?.location)
            setDetailContentText(product?.peekContent()?.content)
            if (editMode == EditMode.READ_ONLY) {
                setDetailContentTextColor(ContextCompat.getColor(context, R.color.black))
                setDetailPriceTextColor(ContextCompat.getColor(context, R.color.black))
                setDetailTitleTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    }

    private fun setupClickListeners() {
        binding.detailEditBt.setOnClickListener {
            showConfirmationDialog()
        }
        binding.detailSubmitTv.setOnClickListener {
            viewModel.updateProduct()
        }
        binding.detailBackIv.setOnClickListener {
            if (viewModel.isReadOnlyMode()) {
                findNavController().navigateUp()
            } else {
                showConfirmationDialog()
            }
        }
    }

    private fun setupTextChangeListeners() {
        binding.detailReceiver.setDetailTitleTextChangeListener(createTextChangeListener(TITLE) { it })
        binding.detailReceiver.setDetailPriceTextChangeListener(createTextChangeListener(PRICE) { it.toInt() })
        binding.detailReceiver.setDetailContentTextChangeListener(createTextChangeListener(CONTENT) { it })
    }

    private fun showConfirmationDialog() {
        val dialogMessage = if (viewModel.isReadOnlyMode().not()) {
            getString(R.string.confirmation_dialog_message_edit)
        } else {
            getString(R.string.confirmation_dialog_message_cancel)
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

    private fun createTextChangeListener(
        fieldName: String,
        conversion: (String) -> Any
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val updatedProduct = viewModel.product.value?.let { product ->
                    val convertedValue = try {
                        conversion(s.toString())
                    } catch (e: Exception) {
                        null
                    }
                    when (fieldName) {
                        TITLE -> (convertedValue as? String)?.let { product.peekContent()?.copy(title = it) }
                        PRICE -> (convertedValue as? Int)?.let { product.peekContent()?.copy(price = it) }
                        CONTENT -> (convertedValue as? String)?.let { product.peekContent()?.copy(content = it) }
                        else -> product
                    }
                }
                updatedProduct?.let { viewModel.updateProduct(it as ProductPostItem) }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun TextView.setTextColorRes(@ColorRes colorResId: Int) {
        val color = ContextCompat.getColor(context, colorResId)
        setTextColor(color)
    }

    private fun setDetailTitleTextColor(isMatch: Boolean) {
        val colorResourceId = if (isMatch) R.color.grey_100 else R.color.black
        val textColor = ContextCompat.getColor(requireContext(), colorResourceId)
        binding.detailReceiver.setDetailTitleTextColor(textColor)
    }

    private fun setDetailPriceTextColor(isMatch: Boolean) {
        val colorResourceId = if (isMatch) R.color.grey_100 else R.color.black
        val textColor = ContextCompat.getColor(requireContext(), colorResourceId)
        binding.detailReceiver.setDetailPriceTextColor(textColor)
    }

    private fun setDetailContentTextColor(isMatch: Boolean) {
        val colorResourceId = if (isMatch) R.color.grey_100 else R.color.black
        val textColor = ContextCompat.getColor(requireContext(), colorResourceId)
        binding.detailReceiver.setDetailContentTextColor(textColor)
    }

    private fun observeEditMode() {
        viewModel.editMode.observe(viewLifecycleOwner) { editMode ->
            setEditMode(editMode.peekContent())
        }
    }

    private fun observeNicknameResponse() {
        viewModel.nickname.observe(viewLifecycleOwner, EventObserver { nickname ->
            binding.detailReceiver.setDetailNicknameText(nickname)
        })

        viewModel.nicknameError.observe(viewLifecycleOwner, EventObserver { nicknameError ->
            if (nicknameError) {
                showToast(R.string.error_message_nickname)
            }
        })
    }

    private fun observeProductData() {
        viewModel.product.observe(viewLifecycleOwner, EventObserver {
            binding.detailSubmitTv.setTextColorRes(if (viewModel.isProductModified()) R.color.orange_700 else R.color.grey_300)
            setDetailTitleTextColor(viewModel.isTitleMatch())
            setDetailPriceTextColor(viewModel.isPriceMatch())
            setDetailContentTextColor(viewModel.isContentMatch())
        })
    }

    private fun observeProductUpdate() {
        viewModel.productUpdateCompleted.observe(viewLifecycleOwner, EventObserver { productUpdateCompleted ->
            if (productUpdateCompleted) {
                findNavController().navigateUp()
                showToast(R.string.product_update_success)
            }
        })

        viewModel.productUpdateError.observe(viewLifecycleOwner, EventObserver { productUpdateError ->
            if(productUpdateError) {
                showToast(R.string.error_message_product_update)
            }
        })

        viewModel.productUpdateLoading.observe(viewLifecycleOwner, EventObserver { productUpdateLoading ->
            if (productUpdateLoading) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        })
    }
}
