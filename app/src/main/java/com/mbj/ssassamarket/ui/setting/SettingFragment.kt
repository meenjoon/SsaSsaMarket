package com.mbj.ssassamarket.ui.setting

import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentSettingBinding
import com.mbj.ssassamarket.ui.BaseFragment

class SettingFragment : BaseFragment() {
    override val binding get() = _binding as FragmentSettingBinding
    override val layoutId: Int get() = R.layout.fragment_setting
}
