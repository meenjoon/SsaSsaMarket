package com.mbj.ssassamarket.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_fcv) as NavHostFragment
        val navController = navHostFragment.findNavController()
        setupNavController(navController)
    }

    private fun setupNavController(navController: NavController) {
        binding.mainBottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val shouldShowBottomNavigation = when (destination.id) {
                R.id.logInFragment,
                R.id.splashFragment,
                R.id.settingNicknameFragment,
                R.id.navigation_writing,
                R.id.sellerFragment,
                R.id.buyerFragment,
                R.id.chatDetailFragment -> false
                else -> true
            }
            binding.mainBottomNavigation.visibility = if (shouldShowBottomNavigation) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}
