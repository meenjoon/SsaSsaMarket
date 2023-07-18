package com.mbj.ssassamarket.ui.launcher

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentSplashFragementBinding
import com.mbj.ssassamarket.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSplashFragementBinding
    override val layoutId: Int get() = R.layout.fragment_splash_fragement

    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        viewModel.checkCurrentUserExists()
        observeAccountExistence()
    }

    private fun navigateBasedOnUserState(
        autoLoginState: Boolean,
        currentUserExists: Boolean,
        currentUser: FirebaseUser?
    ) {
        val action = if (!autoLoginState || currentUser == null) {
            SplashFragmentDirections.actionSplashFragmentToLogInFragment()
        } else if (autoLoginState && currentUser != null && currentUserExists) {
            showToast(R.string.setting_nickname_success)
            SplashFragmentDirections.actionSplashFragmentToHomeFragment()
        } else if (autoLoginState && currentUser != null && !currentUserExists) {
            SplashFragmentDirections.actionSplashFragmentToSettingNicknameFragment()
        } else {
            SplashFragmentDirections.actionSplashFragmentToLogInFragment()
        }
        findNavController().navigate(action)
    }

    private fun observeAccountExistence() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isAccountExistsOnServer.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collectLatest { isAccountExistsOnServer ->
                delay(2000)
                if (isAccountExistsOnServer != null) {
                    navigateBasedOnUserState(
                        viewModel.autoLoginState,
                        isAccountExistsOnServer,
                        viewModel.currentUser
                    )
                }
            }
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }
}
