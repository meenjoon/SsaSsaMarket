package com.mbj.ssassamarket.ui.settingnickname

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.FirebaseDataSource
import com.mbj.ssassamarket.databinding.FragmentSettingNicknameBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.Constants.NICKNAME_DUPLICATE
import com.mbj.ssassamarket.util.Constants.NICKNAME_ERROR
import com.mbj.ssassamarket.util.Constants.NICKNAME_REQUEST
import com.mbj.ssassamarket.util.Constants.NICKNAME_VALID
import com.mbj.ssassamarket.util.Constants.SUCCESS
import com.mbj.ssassamarket.util.EventObserver
import com.mbj.ssassamarket.util.ProgressDialogFragment

class SettingNicknameFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSettingNicknameBinding
    override val layoutId: Int get() = R.layout.fragment_setting_nickname
    private var loadingProgressDialog: ProgressDialogFragment? = null
    private val viewModel by viewModels<SettingNicknameViewModel> {
        SettingNicknameViewModel.provideFactory(UserInfoRepository(FirebaseDataSource()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        viewModel.addUserResult.observe(viewLifecycleOwner) { addUserResult ->
            viewModel.handlePostResponse(addUserResult)
        }
        viewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            viewModel.validateNickname()
        }
        viewModel.nicknameErrorMessage.observe(viewLifecycleOwner) { nicknameErrorMessage ->
            when (nicknameErrorMessage) {
                NICKNAME_REQUEST -> {
                    setButtonBackground(R.color.orange_100)
                    binding.settingNicknameTil.error =
                        getString(R.string.setting_nickname_request_nickname)
                }
                NICKNAME_ERROR -> {
                    setButtonBackground(R.color.orange_100)
                    binding.settingNicknameTil.error =
                        getString(R.string.setting_nickname_error_nickname)
                }
                NICKNAME_VALID -> {
                    setButtonBackground(R.color.orange_700)
                    binding.settingNicknameTil.error = null
                }
            }
        }
        viewModel.preUploadCompleted.observe(viewLifecycleOwner) { preUploadCompleted ->
            if (preUploadCompleted == false) {
                if (loadingProgressDialog == null) {
                    loadingProgressDialog = ProgressDialogFragment()
                    loadingProgressDialog?.show(childFragmentManager, "progress_dialog")
                }
            } else {
                loadingProgressDialog?.dismissAllowingStateLoss()
                loadingProgressDialog = null
            }
        }

        viewModel.uploadSuccess.observe(viewLifecycleOwner, EventObserver { uploadSuccess ->
            if (uploadSuccess) {
                navigateToHomeFragment()
            }
        })

        viewModel.responseToastMessage.observe(viewLifecycleOwner, EventObserver { message ->
            when (message) {
                SUCCESS -> showToast(R.string.setting_nickname_success)
                NICKNAME_DUPLICATE -> showToast(R.string.setting_nickname_duplicate)
                NICKNAME_REQUEST -> showToast(R.string.setting_nickname_request_nickname)
                NICKNAME_ERROR -> showToast(R.string.setting_nickname_error_nickname)
            }
        })
    }

    private fun setButtonBackground(colorResId: Int) {
        val color = ContextCompat.getColor(requireContext(), colorResId)
        binding.settingNicknameRegisterBt.setBackgroundColor(color)
    }

    private fun navigateToHomeFragment() {
        val action = SettingNicknameFragmentDirections.actionSettingNicknameFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }
}
