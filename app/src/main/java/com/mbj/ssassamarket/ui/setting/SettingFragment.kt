package com.mbj.ssassamarket.ui.setting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentSettingBinding
import com.mbj.ssassamarket.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment : BaseFragment() {
    override val binding get() = _binding as FragmentSettingBinding
    override val layoutId: Int get() = R.layout.fragment_setting

    private val viewModel: SettingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        observeLogoutSuccess()

        binding.settingLogoutTv.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun observeLogoutSuccess() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLogoutSuccess.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collectLatest { isLogoutSuccess ->
                if (isLogoutSuccess) {
                    navigateToLoginFragment()
                }
            }
        }
    }

    private fun navigateToLoginFragment() {
        val action = SettingFragmentDirections.actionNavigationSettingToLogInFragment()
        findNavController().navigate(action)
    }

    private fun showLogoutConfirmationDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setMessage(getString(R.string.request_logout))

        builder.setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
            dialog.dismiss()
            viewModel.onLogoutButtonClicked()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
