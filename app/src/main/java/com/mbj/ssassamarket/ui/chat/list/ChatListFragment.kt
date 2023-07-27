package com.mbj.ssassamarket.ui.chat.list

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.ChatRoomItem
import com.mbj.ssassamarket.databinding.FragmentChatListBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.common.ChatListClickListener
import com.mbj.ssassamarket.util.Colors
import com.mbj.ssassamarket.util.LocationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatListFragment() : BaseFragment(), ChatListClickListener {
    @Inject
    lateinit var colors: Colors

    override val binding get() = _binding as FragmentChatListBinding
    override val layoutId: Int get() = R.layout.fragment_chat_list

    private lateinit var adapter: ChatListAdapter
    private val viewModel: ChatListViewModel by viewModels()

    private lateinit var locationManager: LocationManager
    private lateinit var requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = LocationManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        viewModel.getChatRooms()
        viewModel.addChatRoomsValueEventListener()

        initializeLocationPermissionLauncher()
        initRecyclerView()
        observeChatRooms()
    }

    override fun onResume() {
        super.onResume()
        handleLocationPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeChatRoomsValueEventListener()
    }

    private fun initRecyclerView() {
        adapter = ChatListAdapter(this, colors)
        binding.chatListRv.adapter = adapter
    }

    private fun handleLocationPermission() {
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

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showLocationPermissionDeniedDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.permission_request))
        builder.setMessage(getString(R.string.chat_list_location_permission))

        builder.setPositiveButton(getString(R.string.permission_positive)) { dialog, _ ->
            dialog.dismiss()
            openAppSettings()
            viewModel.setLocationPermissionChecked(false)
            viewModel.setSystemSettingsExited(true)
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

    private fun observeChatRooms() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatRooms.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { chatRooms ->
                    adapter.submitList(chatRooms)
                }
        }
    }

    override fun onChatRoomClicked(chatRoomItem: ChatRoomItem, otherImageColor: String) {
        val action = ChatListFragmentDirections.actionNavigationChatToChatDetailFragment(chatRoomItem = chatRoomItem, otherImageColor = otherImageColor)
        findNavController().navigate(action)
    }
}
