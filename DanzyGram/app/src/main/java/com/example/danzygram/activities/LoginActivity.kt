package com.example.danzygram.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.danzygram.R
import com.example.danzygram.databinding.ActivityLoginBinding
import com.example.danzygram.util.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already logged in
        if (FirebaseUtil.isUserLoggedIn()) {
            startMainActivity()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button click
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // Forgot password click
        binding.forgotPassword.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                showSnackbar(getString(R.string.error_email_required))
            }
        }

        // Sign up prompt click
        binding.signupPrompt.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.error_email_required)
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = getString(R.string.error_invalid_email)
            return false
        }
        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.error_password_required)
            return false
        }
        if (password.length < 6) {
            binding.passwordLayout.error = getString(R.string.error_password_too_short)
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                FirebaseUtil.getAuth().signInWithEmailAndPassword(email, password).await()
                startMainActivity()
            } catch (e: Exception) {
                showLoading(false)
                showSnackbar(e.localizedMessage ?: getString(R.string.error_login))
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                FirebaseUtil.getAuth().sendPasswordResetEmail(email).await()
                showSnackbar(getString(R.string.password_reset_email_sent))
                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                showSnackbar(e.localizedMessage ?: getString(R.string.error_password_reset))
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
        binding.emailEditText.isEnabled = !show
        binding.passwordEditText.isEnabled = !show
        binding.forgotPassword.isEnabled = !show
        binding.signupPrompt.isEnabled = !show
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}