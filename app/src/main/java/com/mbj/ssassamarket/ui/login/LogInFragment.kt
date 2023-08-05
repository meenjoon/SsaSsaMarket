package com.mbj.ssassamarket.ui.login

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mbj.ssassamarket.BuildConfig
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentLogInBinding
import com.mbj.ssassamarket.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogInFragment : BaseFragment() {

    override val binding get() = _binding as FragmentLogInBinding
    override val layoutId: Int get() = R.layout.fragment_log_in

    private lateinit var oneTapClient: SignInClient
    private lateinit var auth: FirebaseAuth

    private lateinit var googleOneTapSignInLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var googleSignInLauncherIdentity: ActivityResultLauncher<IntentSenderRequest>

    private val viewModel: LogInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeSignInClients()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        observeAutoLoginAndAccountExistence()
        binding.logInBt.setOnClickListener {
            signInWithGoogleOneTap()
        }
    }

    private fun initializeSignInClients() {
        oneTapClient = Identity.getSignInClient(requireContext())
        auth = FirebaseAuth.getInstance()

        googleOneTapSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                handleGoogleOneTapSignInResult(result.resultCode, result.data)
            }
        googleSignInLauncherIdentity =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                handleGoogleSignInIdentityResult(result.resultCode, result.data)
            }
    }

    private fun handleGoogleOneTapSignInResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val googleIdToken = credential.googleIdToken
            if (googleIdToken != null) {
                firebaseAuthWithGoogle(googleIdToken)
            } else {
                showToast(R.string.log_in_failure)
            }
        } else {
            showToast(R.string.log_in_cancle)
        }
    }

    private fun handleGoogleSignInIdentityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val credential = Identity.getSignInClient(requireActivity())
                    .getSignInCredentialFromIntent(data)
                val googleIdToken = credential.googleIdToken
                if (googleIdToken != null) {
                    firebaseAuthWithGoogle(googleIdToken)
                } else {
                    showToast(R.string.log_in_failure)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
            }
        } else {
            showToast(R.string.log_in_cancle)
        }
    }

    private fun signInWithGoogleOneTap() {
        oneTapClient.beginSignIn(getGoogleOneTapSignInOptions())
            .addOnSuccessListener(requireActivity()) { result ->
                val intentSenderRequest =
                    IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                googleOneTapSignInLauncher.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to begin one tap sign-in", e)
                handleOneTapException(e)
            }
    }

    private fun getGoogleOneTapSignInOptions(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    private fun signInWithGoogleByIdentity() {
        val request = GetSignInIntentRequest.builder()
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .build()

        Identity.getSignInClient(requireActivity())
            .getSignInIntent(request)
            .addOnSuccessListener { result ->
                try {
                    googleSignInLauncherIdentity.launch(
                        IntentSenderRequest.Builder(result.intentSender).build()
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Google Sign-in failed")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Google Sign-in failed", e)
            }
    }

    private fun handleOneTapException(exception: Exception) {
        when (exception) {
            is ApiException -> {
                Log.e(TAG, "Failed to begin one tap sign-in(ApiException)", exception)
                when (exception.statusCode) {
                    CommonStatusCodes.CANCELED -> signInWithGoogleByIdentity()
                    else -> signInWithGoogleByIdentity()
                }
            }
            else -> {
                Log.e(TAG, "Failed to begin one tap sign-in(Exception)", exception)
                signInWithGoogleByIdentity()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModel.currentUserExists()
                } else {
                    Log.e(TAG, "Firebase authentication failed", task.exception)
                    when (task.exception?.message) {
                        FIREBASE_AUTH_EXCEPTION_NETWORK -> showToast(R.string.log_in_error_network)
                        FIREBASE_AUTH_EXCEPTION_NETWORK2 -> showToast(R.string.log_in_error_network)
                        else -> showToast(R.string.log_in_error_firebase)
                    }
                }
            }
    }

    private fun observeAutoLoginAndAccountExistence() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.autoLoginEnabled.collectLatest { autoLoginEnabled ->
                        viewModel.userPreferenceRepository.saveAutoLoginState(autoLoginEnabled)
                    }
                }
                launch {
                    viewModel.isAccountExistsOnServer.collectLatest { isAccountExistsOnServer ->
                        if (isAccountExistsOnServer != null) {
                            if (isAccountExistsOnServer) {
                                showToast(R.string.setting_nickname_success)
                                navigateToHomeFragment()
                            } else {
                                navigateToSettingNicknameFragment()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToSettingNicknameFragment() {
        val action = LogInFragmentDirections.actionLogInFragmentToSettingNicknameFragment()
        findNavController().navigate(action)
    }

    private fun navigateToHomeFragment() {
        val action = LogInFragmentDirections.actionLogInFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "LogInFragment"
        private const val FIREBASE_AUTH_EXCEPTION_NETWORK =
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." //네트워크 오류로 인해 Firebase 인증 실패 오류 메세지
        private const val FIREBASE_AUTH_EXCEPTION_NETWORK2 =
            "com.google.firebase.FirebaseNetworkException: A network error (such as timeout, interrupted connection or unreachable host)" //네트워크 오류로 인해 Firebase 인증 실패 오류 메세지
    }
}
