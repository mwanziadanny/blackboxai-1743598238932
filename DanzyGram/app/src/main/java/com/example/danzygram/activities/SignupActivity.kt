package com.example.danzygram.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.danzygram.R
import com.example.danzygram.data.User
import com.example.danzygram.databinding.ActivitySignupBinding
import com.example.danzygram.util.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Signup button click
        binding.signupButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInput(username, email, password, confirmPassword)) {
                createAccount(username, email, password)
            }
        }

        // Login prompt click
        binding.loginPrompt.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // Reset errors
        binding.usernameLayout.error = null
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null

        // Username validation
        if (username.isEmpty()) {
            binding.usernameLayout.error = getString(R.string.error_username_required)
            return false
        }
        if (username.length < 3) {
            binding.usernameLayout.error = getString(R.string.error_username_too_short)
            return false
        }

        // Email validation
        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.error_email_required)
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = getString(R.string.error_invalid_email)
            return false
        }

        // Password validation
        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.error_password_required)
            return false
        }
        if (password.length < 6) {
            binding.passwordLayout.error = getString(R.string.error_password_too_short)
            return false
        }
        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.error_passwords_dont_match)
            return false
        }

        return true
    }

    private fun createAccount(username: String, email: String, password: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                // Create authentication account
                val result = FirebaseUtil.getAuth()
                    .createUserWithEmailAndPassword(email, password)
                    .await()

                // Create user document in Firestore
                val userId = result.user?.uid ?: throw Exception("User ID is null")
                val user = User(
                    userId = userId,
                    username = username,
                    email = email
                )

                FirebaseUtil.getUsersCollection()
                    .document(userId)
                    .set(user)
                    .await()

                // Start MainActivity
                startMainActivity()
            } catch (e: Exception) {
                showLoading(false)
                showSnackbar(e.localizedMessage ?: getString(R.string.error_signup))
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.signupButton.isEnabled = !show
        binding.usernameEditText.isEnabled = !show
        binding.emailEditText.isEnabled = !show
        binding.passwordEditText.isEnabled = !show
        binding.confirmPasswordEditText.isEnabled = !show
        binding.loginPrompt.isEnabled = !show
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}