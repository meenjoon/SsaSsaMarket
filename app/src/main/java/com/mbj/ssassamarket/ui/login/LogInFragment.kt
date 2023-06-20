package com.mbj.ssassamarket.ui.login

import com.mbj.ssassamarket.ui.BaseFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mbj.ssassamarket.databinding.FragmentLogInBinding

class LogInFragment : BaseFragment<FragmentLogInBinding>() {

    private lateinit var binding: FragmentLogInBinding

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLogInBinding {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding
    }
}
