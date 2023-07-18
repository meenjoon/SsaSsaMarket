package com.mbj.ssassamarket.ui.settingnickname

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentSettingNicknameBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.Constants.NICKNAME_DUPLICATE
import com.mbj.ssassamarket.util.Constants.NICKNAME_ERROR
import com.mbj.ssassamarket.util.Constants.NICKNAME_REQUEST
import com.mbj.ssassamarket.util.Constants.SUCCESS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingNicknameFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSettingNicknameBinding
    override val layoutId: Int get() = R.layout.fragment_setting_nickname

    private val viewModel: SettingNicknameViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        viewModel.validateNickname()
        observeSettingNicknameStatus()
    }

    fun observeSettingNicknameStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isCompleted.collectLatest { isCompleted ->
                        if (isCompleted) {
                            navigateToHomeFragment()
                            showToast(R.string.setting_nickname_success)
                        }
                    }
                }
                launch {
                    viewModel.responseToastMessage.collectLatest { responseToastMessage ->
                        when (responseToastMessage) {
                            SUCCESS -> showToast(R.string.setting_nickname_success)
                            NICKNAME_DUPLICATE -> showToast(R.string.setting_nickname_duplicate)
                            NICKNAME_REQUEST -> showToast(R.string.setting_nickname_request_nickname)
                            NICKNAME_ERROR -> showToast(R.string.setting_nickname_error_nickname)
                        }
                    }
                }
            }
        }
    }

    private fun navigateToHomeFragment() {
        val action = SettingNicknameFragmentDirections.actionSettingNicknameFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }
}
