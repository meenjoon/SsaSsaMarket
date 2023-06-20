package com.mbj.ssassamarket.ui.login

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import com.mbj.ssassamarket.ui.BaseFragment
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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

class LogInFragment : BaseFragment() {

    override val binding get() = _binding as FragmentLogInBinding
    override val layoutId: Int get() = R.layout.fragment_log_in

    private lateinit var oneTapClient: SignInClient
    private lateinit var auth: FirebaseAuth

    private lateinit var googleOneTabSignInLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var googleSignInLauncherIdentity: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeSignInClients()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logInBt.setOnClickListener {
            signInWithGoogleOneTap()
        }
    }

    private fun initializeSignInClients() {
        oneTapClient = Identity.getSignInClient(requireContext())
        auth = FirebaseAuth.getInstance()

        googleOneTabSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            handleGoogleOneTabSignInResult(result.resultCode, result.data)
        }
        googleSignInLauncherIdentity = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            handleGoogleSignInIdentityResult(result.resultCode, result.data)
        }
    }

    private fun handleGoogleOneTabSignInResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val intent = data
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
            if (googleIdToken != null) {
                firebaseAuthWithGoogle(googleIdToken)
            } else {
                Toast.makeText(requireContext(), R.string.log_in_failure, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), R.string.log_in_cancle, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), R.string.log_in_failure, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
            }
        } else {
            Toast.makeText(requireContext(), R.string.log_in_cancle, Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogleOneTap() {
        oneTapClient.beginSignIn(getGoogleOneTabSignInOptions())
            .addOnSuccessListener(requireActivity()) { result ->
                val intentSenderRequest =
                    IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                googleOneTabSignInLauncher.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to begin one tab sign-in", e)
                handleOneTabException(e)
            }
    }

    private fun getGoogleOneTabSignInOptions(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(true)
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

    private fun handleOneTabException(exception: Exception) {
        when (exception) {

            is ApiException -> {
                Log.e(TAG, "Failed to begin one tab sign-in(ApiException)", exception)
                when (exception.statusCode) {
                    CommonStatusCodes.CANCELED ->  signInWithGoogleByIdentity()
                    else ->  signInWithGoogleByIdentity()
                }
            }
            else -> {
                Log.e(TAG, "Failed to begin one tab sign-in(Exception)", exception)
                signInWithGoogleByIdentity()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateToHomeFragment()
                    Toast.makeText(requireContext(), getString(R.string.log_in_success), Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Firebase authentication failed", task.exception)
                    when (task.exception?.message) {
                        FIREBASE_AUTH_EXCEPTION_NETWORK -> Toast.makeText(requireContext(), R.string.log_in_error_network, Toast.LENGTH_SHORT).show()
                        FIREBASE_AUTH_EXCEPTION_NETWORK2 -> Toast.makeText(requireContext(), R.string.log_in_error_network, Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(requireContext(), R.string.log_in_error_firebase, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun navigateToHomeFragment() {
        val action = LogInFragmentDirections.actionLogInFragmentToSettingNicknameFragment()
        findNavController().navigate(action)
    }

    companion object {
        private const val TAG = "LogInFragment"
        private const val FIREBASE_AUTH_EXCEPTION_NETWORK =
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." //네트워크 오류로 인해 Firebase 인증 실패 오류 메세지
        private const val FIREBASE_AUTH_EXCEPTION_NETWORK2 =
            "com.google.firebase.FirebaseNetworkException: A network error (such as timeout, interrupted connection or unreachable host)" //네트워크 오류로 인해 Firebase 인증 실패 오류 메세지
    }
}

