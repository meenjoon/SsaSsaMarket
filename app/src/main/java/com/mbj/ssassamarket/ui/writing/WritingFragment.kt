package com.mbj.ssassamarket.ui.writing

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import android.Manifest
import android.os.Build
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.databinding.FragmentWritingBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.common.GalleryClickListener
import com.mbj.ssassamarket.ui.common.ImageRemoveListener

class WritingFragment : BaseFragment() {
    override val binding get() = _binding as FragmentWritingBinding
    override val layoutId: Int get() = R.layout.fragment_writing

    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var attachedImageAdapter: AttachedImageAdapter

    private val viewModel: WritingViewModel by viewModels()

    private val onGallerySelectedListener = object : GalleryClickListener {
        override fun onGalleryClick() {
            openGallery()
        }
    }
    private val onImageContentRemoveListener = object : ImageRemoveListener {
        override fun onImageRemoveListener(imageContent: ImageContent) {
            viewModel.removeSelectedImage(imageContent)
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

    private val pickMultipleVisualMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
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
    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
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

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted.not()) {
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (!shouldShowRationale) {
                    showLocationPermissionDeniedDialog()
                } else {
                    findNavController().navigateUp()
                    showToast(R.string.gallery_permission_cancel)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySpinner()
        setTouchInterceptHandling()
        setupAdapters()
        setupRecyclerView()
        observeSelectedImageContent()
        handleBackButtonClick()
        checkLocationPermission()
    }

    private fun setupAdapters() {
        attachedImageAdapter = AttachedImageAdapter(onImageContentRemoveListener)
        galleryAdapter = GalleryAdapter(onGallerySelectedListener)
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

    private fun observeSelectedImageContent() {
        viewModel.selectedImageList.observe(viewLifecycleOwner) { imageList ->
            attachedImageAdapter.submitList(imageList)
            galleryAdapter.updateSelectedImageContentSize(imageList.size)
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
                        view.setTextColor(Color.BLACK)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.writingCategorySpinner.setSelection(0)
            }
        }
    }

    private fun setTouchInterceptHandling() {
        binding.writingContentTiet.setOnTouchListener { view, motionEvent ->
            if (view.id == R.id.writing_content_tiet) {
                view.parent.requestDisallowInterceptTouchEvent(true)
                when (motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                permissionResultLauncher.launch(permission)
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

    private fun checkLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(permission)
        }
    }

    private fun showLocationPermissionDeniedDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.permission_request))
        builder.setMessage(getString(R.string.location_permission_content))

        builder.setPositiveButton(getString(R.string.permission_positive)) { dialog, _ ->
            dialog.dismiss()
            openAppSettings()
        }
        builder.setNegativeButton(getString(R.string.permission_negative)) { dialog, _ ->
            dialog.dismiss()
            findNavController().navigateUp()
            showToast(R.string.location_permission_cancel)
        }

        val dialog = builder.create()
        dialog.show()
    }
}
