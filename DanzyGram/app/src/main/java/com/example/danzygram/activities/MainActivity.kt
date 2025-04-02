package com.example.danzygram.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.danzygram.R
import com.example.danzygram.databinding.ActivityMainBinding
import com.example.danzygram.util.FirebaseUtil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is logged in, if not, redirect to LoginActivity
        if (!FirebaseUtil.isUserLoggedIn()) {
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup bottom navigation with nav controller
        binding.bottomNavigation.setupWithNavController(navController)

        // Handle reselection
        binding.bottomNavigation.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    // Scroll to top or refresh
                    navController.popBackStack(R.id.homeFragment, false)
                }
                R.id.searchFragment -> {
                    // Clear search or refresh
                    navController.popBackStack(R.id.searchFragment, false)
                }
                R.id.uploadFragment -> {
                    // Do nothing or refresh
                    navController.popBackStack(R.id.uploadFragment, false)
                }
                R.id.profileFragment -> {
                    // Scroll to top or refresh
                    navController.popBackStack(R.id.profileFragment, false)
                }
            }
        }

        // Handle destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Show/hide bottom navigation based on destination
            when (destination.id) {
                R.id.editProfileFragment -> binding.bottomNavigation.hide()
                else -> binding.bottomNavigation.show()
            }
        }
    }
}