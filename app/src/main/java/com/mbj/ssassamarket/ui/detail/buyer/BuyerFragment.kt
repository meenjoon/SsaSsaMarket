package com.mbj.ssassamarket.ui.detail.buyer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentBuyerBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.detail.BannerAdapter
import com.mbj.ssassamarket.util.Constants
import com.mbj.ssassamarket.util.EventObserver
import com.mbj.ssassamarket.util.LocationManager
import com.mbj.ssassamarket.util.ProgressDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BuyerFragment : BaseFragment() {

    override val binding get() = _binding as FragmentBuyerBinding
    override val layoutId: Int get() = R.layout.fragment_buyer

    private val args: BuyerFragmentArgs by navArgs()
    private val viewModel: BuyerViewModel by viewModels()

    private lateinit var locationManager: LocationManager
    private lateinit var bannerAdapter: BannerAdapter

    private var progressDialog: ProgressDialogFragment? = null

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                onLocationPermissionGranted()
            } else {
                showPermissionPermanentlyDeniedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = LocationManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setupViewModel()
        setupViews()
    }

    private fun setupViewModel() {
        viewModel.setOtherUserId(args.product.id)
        viewModel.initializeProduct(args.postId, args.product)
        viewModel.getProductNickname()
        viewModel.checkProductInFavorites()

        viewModel.chatRoomId.observe(viewLifecycleOwner, EventObserver { chatRoomId ->
            viewModel.otherUserId.value?.let { otherUserId ->
                navigateToChatDetailFragment(chatRoomId, otherUserId.peekContent())
            }
        })

        viewModel.isLiked.observe(viewLifecycleOwner, EventObserver { isLinked ->
            viewModel.handleFavoriteResponse()
        })

        viewModel.productFavoriteCompleted.observe(viewLifecycleOwner, EventObserver { completed ->
            if (completed) {
                hideLoadingDialog()
            } else {
                showLoadingDialog()
            }
        })
    }

    private fun setupViews() {
        setupViewPager()
        with(binding) {
            detailBuyerChatBt.setOnClickListener { onBuyerChatButtonClicked() }
            detailBuyerBuyBt.setOnClickListener { onBuyerBuyButtonClicked() }
            detailBackIv.setOnClickListener { navigateUp() }
            val productPostItem = viewModel!!.getProductPostItem()
            bannerAdapter.submitList(productPostItem?.imageLocations)
        }
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

    private fun requestLocationPermission() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                fineLocationPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(fineLocationPermission)
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                coarseLocationPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(coarseLocationPermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestLocationPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun showPermissionPermanentlyDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_request)
            .setMessage(R.string.buyer_chat_buy_permission)
            .setPositiveButton(R.string.permission_positive) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.permission_negative, null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun onLocationPermissionGranted() {
        viewModel.onChatButtonClicked(
            getString(R.string.test_seller_name),
            getString(R.string.test_seller_location)
        )
    }

    private fun onBuyerChatButtonClicked() {
        if (locationManager.isAnyLocationPermissionGranted()) {
            onLocationPermissionGranted()
        } else {
            requestLocationPermission()
        }
    }

    private fun onBuyerBuyButtonClicked() {
        if (locationManager.isAnyLocationPermissionGranted()) {
            onLocationPermissionGranted()
        } else {
            requestLocationPermission()
        }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialogFragment()
        progressDialog?.show(childFragmentManager, Constants.PROGRESS_DIALOG)
    }

    private fun hideLoadingDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun navigateToChatDetailFragment(chatRoomId: String, otherId: String) {
        val action = BuyerFragmentDirections.actionBuyerFragmentToChatDetailFragment(
            chatRoomId = chatRoomId,
            otherId = otherId
        )
        findNavController().navigate(action)
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}
