package com.mbj.ssassamarket.ui.writing

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.databinding.FragmentWritingBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.common.GalleryClickListener
import com.mbj.ssassamarket.ui.common.ImageRemoveListener
import com.mbj.ssassamarket.util.Constants.PROGRESS_DIALOG
import com.mbj.ssassamarket.util.LocateFormat
import com.mbj.ssassamarket.util.LocationManager
import com.mbj.ssassamarket.util.ProgressDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint

@AndroidEntryPoint
class WritingFragment : BaseFragment(), LocationManager.LocationUpdateListener, GalleryClickListener, ImageRemoveListener {

    override val binding get() = _binding as FragmentWritingBinding
    override val layoutId: Int get() = R.layout.fragment_writing

    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var attachedImageAdapter: AttachedImageAdapter

    private lateinit var locationManager: LocationManager
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickMultipleVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var requestGalleryPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private var progressDialog: ProgressDialogFragment? = null

    private val viewModel: WritingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeGalleryLauncher()
        initializePickMultipleVisualMediaLauncher()
        initializeGalleryPermissionLauncher()
        initializeGalleryLauncher()
        initializeLocationPermissionLauncher()
        locationManager = LocationManager(requireContext(), 10000L, 10000.0F, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setupCategorySpinner()
        setupAdapters()
        setupRecyclerView()
        handleBackButtonClick()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isCompleted.collectLatest { isCompleted ->
                        if (isCompleted) {
                            hideLoadingDialog()
                        } else {
                            showLoadingDialog()
                        }
                    }
                }
                launch {
                    viewModel.isSuccess.collectLatest { isSuccess ->
                        if (isSuccess) {
                            findNavController().navigateUp()
                        }
                    }
                }
                launch {
                    viewModel.selectedImageList.collectLatest { selectedImageList ->
                        attachedImageAdapter.submitList(selectedImageList)
                        galleryAdapter.updateSelectedImageContentSize(selectedImageList.size)
                    }
                }
                launch {
                    viewModel.toastMessageId.collectLatest { toastMessageId ->
                        showToast(toastMessageId)
                    }
                }
                launch {
                    viewModel.isPostError.collectLatest { isPostError ->
                        showToast(isPostError)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handleLocationPermissionAndTracking()
    }

    override fun onPause() {
        super.onPause()
        locationManager.stopLocationTracking()
    }

    private fun setupAdapters() {
        attachedImageAdapter = AttachedImageAdapter(this)
        galleryAdapter = GalleryAdapter(this)
    }

    private fun setupRecyclerView() {
        val concatAdapter = ConcatAdapter(galleryAdapter, attachedImageAdapter)
        binding.writingRv.adapter = concatAdapter
    }


    private fun setupCategorySpinner() {
        val categoriesWithHint = getCategoriesWithHint()
        val adapter = createSpinnerAdapter(categoriesWithHint)

        with(binding.writingCategorySpinner) {
            this.adapter = adapter
            onItemSelectedListener = createItemSelectedListener()
        }
    }

    private fun handleBackButtonClick() {
        binding.writingBackIv.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun getCategoriesWithHint(): Array<String> {
        val categories = resources.getStringArray(R.array.category_list)
        val categoryHint = getString(R.string.category_hint)
        return arrayOf(categoryHint) + categories
    }

    private fun createSpinnerAdapter(categories: Array<String>): ArrayAdapter<String> {
        val adapter = object : ArrayAdapter<String>(
            requireActivity(),
            R.layout.spinner_item_category,
            categories
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.isEnabled = position != 0 // Enable/disable the item in the dropdown view
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun createItemSelectedListener(): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position) as String

                if (selectedItem != getString(R.string.category_hint)) {
                    if (view is TextView) {
                        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_category_spinner))
                    }
                }
                viewModel.setCategoryLabel(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.writingCategorySpinner.setSelection(0)
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        }
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pickMultipleVisualMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                galleryLauncher.launch(intent)
            } else {
                requestGalleryPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showGalleryPermissionDeniedDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.permission_request))
        builder.setMessage(getString(R.string.gallery_permission_content))

        builder.setPositiveButton(getString(R.string.permission_positive)) { dialog, _ ->
            dialog.dismiss()
            openAppSettings()
        }
        builder.setNegativeButton(getString(R.string.permission_negative)) { dialog, _ ->
            dialog.dismiss()
            showToast(R.string.gallery_permission_cancel)
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showLocationPermissionDeniedDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.permission_request))
        builder.setMessage(getString(R.string.location_permission_content))

        builder.setPositiveButton(getString(R.string.permission_positive)) { dialog, _ ->
            dialog.dismiss()
            openAppSettings()
            viewModel.setSystemSettingsExited(true)
            viewModel.setLocationPermissionChecked(false)
        }
        builder.setNegativeButton(getString(R.string.permission_negative)) { dialog, _ ->
            dialog.dismiss()
            findNavController().navigateUp()
            showToast(R.string.location_permission_cancel)
        }

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onLocationUpdated(latitude: Double?, longitude: Double?) {
        if (latitude != null && longitude != null) {
            val latLngString = "$latitude $longitude"
            viewModel.setLatLng(latLngString)
            val mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
            val reverseGeoCoder = locationManager.createReverseGeoCoder(requireActivity(),mapPoint) { addressString ->
                val location = LocateFormat.getSelectedAddress(addressString, 2)
                viewModel.setLocation(location)
            }
            reverseGeoCoder.startFindingAddress()
        }
    }

    private fun handleLocationPermissionAndTracking() {
        if (!viewModel.isLocationPermissionChecked()) {
            if (viewModel.isSystemSettingsExited() && !locationManager.isAnyLocationPermissionGranted()) {
                // 시스템 설정에서 돌아온 경우이지만 위치 권한이 허용되지 않은 경우
                findNavController().navigateUp()
            } else if (viewModel.isSystemSettingsExited() && locationManager.isAnyLocationPermissionGranted()) {
                // 시스템 설정에서 돌아온 경우이고 위치 권한이 허용된 경우
            } else {
                // 처음 진입하는 경우 위치 권한 체크
                locationManager.checkLocationPermission(requestLocationPermissionLauncher)
                viewModel.setLocationPermissionChecked(true)
            }
        }
        locationManager.startLocationTracking()
    }

    private fun initializeGalleryLauncher()  {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val clipData = data.clipData
                    val imageList = mutableListOf<ImageContent>()
                    if (clipData != null) {
                        // 다중 이미지 선택
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            val fileName = getFileName(uri)
                            if (fileName != null) {
                                val image = ImageContent(uri, fileName)
                                imageList.add(image)
                            }
                        }
                    } else {
                        // 단일 이미지 선택
                        val uri = data.data
                        if (uri != null) {
                            val fileName = getFileName(uri)
                            if (fileName != null) {
                                val image = ImageContent(uri, fileName)
                                imageList.add(image)
                            }
                        }
                    }
                    viewModel.handleGalleryResult(imageList)
                }
            }
        }
    }

    private fun initializePickMultipleVisualMediaLauncher() {
        pickMultipleVisualMediaLauncher = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            if (uris.isNotEmpty()) {
                val imageList = mutableListOf<ImageContent>()
                for (uri in uris) {
                    val fileName = getFileName(uri)
                    if (fileName != null) {
                        val image = ImageContent(uri, fileName)
                        imageList.add(image)
                    }
                }
                viewModel.handleGalleryResult(imageList)
            }
        }
    }

    private fun initializeGalleryPermissionLauncher() {
        requestGalleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if (!shouldShowRationale) {
                    showGalleryPermissionDeniedDialog()
                } else {
                    showToast(R.string.gallery_permission_cancel)
                }
            }
        }
    }

    private fun initializeLocationPermissionLauncher() {
        requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, Boolean> ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (!(fineLocationGranted || coarseLocationGranted)) {
                // 위치 권한이 거부된 경우
                val shouldShowRationaleFine = ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                val shouldShowRationaleCoarse = ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (!shouldShowRationaleFine || !shouldShowRationaleCoarse) {
                    // 권한 요청이 다시 보여지지 않는 경우
                    showLocationPermissionDeniedDialog()
                } else {
                    // 권한 요청이 다시 보여지는 경우
                    findNavController().navigateUp()
                    showToast(R.string.location_permission_cancel)
                }
            }
        }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialogFragment()
        progressDialog?.show(childFragmentManager, PROGRESS_DIALOG)
    }

    private fun hideLoadingDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onGalleryClick() {
        openGallery()
    }

    override fun onImageRemoveListener(imageContent: ImageContent) {
        viewModel.removeSelectedImage(imageContent)
    }
}
