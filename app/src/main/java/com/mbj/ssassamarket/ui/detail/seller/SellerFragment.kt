package com.mbj.ssassamarket.ui.detail.seller

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
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
import com.mbj.ssassamarket.util.DateFormat.getFormattedElapsedTime
import com.mbj.ssassamarket.util.EventObserver
import com.mbj.ssassamarket.util.ProgressDialogFragment
import com.mbj.ssassamarket.util.TextFormat.convertToCurrencyFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerFragment : BaseFragment() {

    override val binding by lazy { FragmentSellerBinding.inflate(layoutInflater) }
    override val layoutId: Int = R.layout.fragment_seller

    private val args: SellerFragmentArgs by navArgs()
    private val viewModel: SellerViewModel by viewModels()

    private var progressDialog: ProgressDialogFragment? = null
    private lateinit var bannerAdapter: BannerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupUI()
        observeData()
        setupViewPager()
        setupClickListeners()
        setupTextChangeListeners()
    }

    private fun initViewModel() {
        viewModel.initializeProduct(args.product)
    }

    private fun setupUI() {
        binding.detailBackIv.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeData() {
        viewModel.editMode.observe(viewLifecycleOwner) { editMode ->
            setEditMode(editMode.peekContent())
        }

        viewModel.getProductNickname()

        viewModel.nickname.observe(viewLifecycleOwner, EventObserver { nickname ->
            binding.detailReceiver.setDetailNicknameText(nickname)
            viewModel.handlePostResponse(nickname)
        })

        viewModel.productNicknameCompleted.observe(viewLifecycleOwner, EventObserver { productNicknameCompleted ->
            if (!productNicknameCompleted) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        })

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

    private fun setEditMode(editMode: EditMode) {
        val product = viewModel.product.value
        bannerAdapter.submitList(product?.peekContent()?.imageLocations)
        binding.detailEditAblTv.visibility =
            if (editMode == EditMode.EDIT) View.VISIBLE else View.INVISIBLE
        binding.detailSubmitTv.visibility =
            if (editMode == EditMode.EDIT) View.VISIBLE else View.INVISIBLE
        binding.detailEditBt.text = if (editMode == EditMode.EDIT) {
            getString(R.string.seller_edit_cancel)
        } else {
            getString(R.string.seller_edit)
        }
        binding.detailReceiver.apply {
            setDetailContentEnabled(editMode == EditMode.EDIT)
            setDetailPriceEnabled(editMode == EditMode.EDIT)
            setDetailTitleEnabled(editMode == EditMode.EDIT)
            setDetailTitleText(product?.peekContent()?.title)
            setDetailTimeText(product?.peekContent()?.createdDate?.let { getFormattedElapsedTime(it) })
            setDetailPriceText(product?.peekContent()?.price?.let { convertToCurrencyFormat(it, requireContext()) })
            setLocation(product?.peekContent()?.location)
            setDetailContentText(product?.peekContent()?.content)
        }
    }

    private fun setupClickListeners() {
        binding.detailEditBt.setOnClickListener {
            showConfirmationDialog()
        }
        binding.detailSubmitTv.setOnClickListener {
            TODO("상품 정보 수정 네트워크 통신 구현")
        }
    }

    private fun setupTextChangeListeners() {
        binding.detailReceiver.setDetailTitleTextChangeListener(createTextChangeListener(TITLE) { it })
        binding.detailReceiver.setDetailPriceTextChangeListener(createTextChangeListener(PRICE) { it.toInt() })
        binding.detailReceiver.setDetailContentTextChangeListener(createTextChangeListener(CONTENT) { it })
    }

    private fun showConfirmationDialog() {
        val currentEditMode = viewModel.editMode.value?.peekContent()
        val dialogMessage = if (currentEditMode == EditMode.EDIT) {
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
}
