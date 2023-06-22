package com.mbj.ssassamarket.ui.launcher

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.source.UserInfoRepository
import com.mbj.ssassamarket.data.source.remote.FirebaseDataSource
import com.mbj.ssassamarket.databinding.FragmentSplashFragementBinding
import com.mbj.ssassamarket.ui.BaseFragment

class SplashFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSplashFragementBinding
    override val layoutId: Int get() = R.layout.fragment_splash_fragement

    private val viewModel by viewModels<SplashViewModel> {
        SplashViewModel.provideFactory(UserInfoRepository(FirebaseDataSource()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkCurrentUserExists()

        binding.splashLav.postDelayed({
            viewModel.getUserResult.observe(viewLifecycleOwner) { currentUserExists ->
                navigateBasedOnUserState(
                    viewModel.autoLoginState,
                    currentUserExists,
                    viewModel.currentUser
                )
            }
        }, 2000)
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

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }
}
