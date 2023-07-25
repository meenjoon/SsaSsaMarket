package com.mbj.ssassamarket.ui.home


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.Category
import com.mbj.ssassamarket.databinding.FragmentHomeBinding
import com.mbj.ssassamarket.ui.BaseFragment

class HomeFragment : BaseFragment() {

    override val binding get() = _binding as FragmentHomeBinding
    override val layoutId: Int get() = R.layout.fragment_home

    private val homeCategories = Category.values().toList()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeVp2.adapter = HomePagerStateAdapter(this, homeCategories)
        TabLayoutMediator(binding.homeTl, binding.homeVp2) { tab, position ->
            tab.text = homeCategories[position].label
        }.attach()

        requestNotificationPermission()
        askNotificationPermission()
    }

    private fun requestNotificationPermission() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationalDialog()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showPermissionRationalDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle(getString(R.string.permission_request))
            builder.setMessage(getString(R.string.notification_permission))

            builder.setPositiveButton(getString(R.string.permission_positive)) { dialog, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                dialog.dismiss()
            }
            builder.setNegativeButton(getString(R.string.permission_negative)) { dialog, _ ->
                dialog.dismiss()
            }.show()
        }
    }
}
