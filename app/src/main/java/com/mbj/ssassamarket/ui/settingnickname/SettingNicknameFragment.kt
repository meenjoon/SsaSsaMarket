package com.mbj.ssassamarket.ui.settingnickname

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentSettingNicknameBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.Constants.NICKNAME_PATTERN

class SettingNicknameFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSettingNicknameBinding
    override val layoutId: Int get() = R.layout.fragment_setting_nickname

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        binding.settingNicknameRegisterBt.setOnClickListener {
            if (validateNickname()) {
                navigateToHomeFragment()
            } else {
                binding.settingNicknameTiet.requestFocus()
            }
        }

        binding.settingNicknameTiet.doAfterTextChanged {
            if (validateNickname()) {
                setButtonBackground(R.color.orange_700)
            } else {
                setButtonBackground(R.color.orange_100)
            }
        }
    }

    private fun validateNickname(): Boolean {
        val value: String = binding.settingNicknameTiet.text.toString()

        return when {
            value.isEmpty() -> {
                binding.settingNicknameTil.error = getString(R.string.setting_nickname_request_nickname)
                false
            }
            !value.matches(NICKNAME_PATTERN.toRegex()) -> {
                binding.settingNicknameTil.error = getString(R.string.setting_nickname_error_nickname)
                false
            }
            else -> {
                binding.settingNicknameTil.error = null
                true
            }
        }
    }

    private fun setButtonBackground(colorResId: Int) {
        val color = ContextCompat.getColor(requireContext(), colorResId)
        binding.settingNicknameRegisterBt.setBackgroundColor(color)
    }

    private fun navigateToHomeFragment() {
        val action = SettingNicknameFragmentDirections.actionSettingNicknameFragmentToHomeFragment()
        findNavController().navigate(action)
    }
}
